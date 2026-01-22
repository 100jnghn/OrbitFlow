package com.finalproj.orbitflow.approval.document.ai.service;

import com.finalproj.orbitflow.approval.document.ai.service.client.DocumentSummaryAiClient;
import com.finalproj.orbitflow.approval.document.ai.entity.DocumentAISummary;
import com.finalproj.orbitflow.approval.document.ai.repository.DocumentAiSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * 결재 문서 AI 요약 및 변경 비교 작업을 비동기로 처리하는 서비스.
 * <p>
 * AI 요약(summary)과 문서 비교(diff)는 외부 AI 서비스 호출을 포함하므로,
 * 요청-응답 흐름과 분리하여 비동기(@Async) 방식으로 실행된다.
 * <p>
 * AI 호출 실패 시 일시적인 네트워크 오류나 타임아웃에 대비하여
 * 제한된 횟수(MAX_ATTEMPTS)만큼 재시도를 수행하며,
 * 재시도 간에는 점진적인 backoff 전략을 적용한다.
 * <p>
 * 모든 AI 처리 결과는 DocumentAISummary 엔티티에 반영되며,
 * 성공 시에는 COMPLETED 상태로, 실패 시에는 FAILED 상태로 상태를 전이한다.
 * 실패 처리(markFailed)는 별도의 트랜잭션(REQUIRES_NEW)으로 수행되어
 * 외부 예외 발생 시에도 상태 변경이 안전하게 반영되도록 설계되었다.
 * <p>
 * 본 서비스는 AI 호출 및 재시도/실패 처리만을 책임하며,
 * 프롬프트 생성이나 요청 흐름 제어는 상위 서비스에서 담당한다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentAiSummaryAsyncService
 * @since : 26. 1. 5. 월요일
 */


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentAiSummaryAsyncService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MILLIS = 1000L;
    private static final int FAIL_MESSAGE_MAX_LENGTH = 1000;


    private final DocumentAiSummaryRepository documentAiSummaryRepository;
    private final DocumentSummaryAiClient documentSummaryAiClient;

    /* =========================================================
     * Public Async APIs
     * ========================================================= */

    /**
     * 문서 요약 생성
     */
    @Async
    @Transactional
    public void generateSummaryAsync(Long summaryId, String prompt) {
        executeWithRetry(
                summaryId,
                prompt,
                documentSummaryAiClient::summarize,
                "SUMMARY"
        );
    }

    /**
     * 문서 diff 생성
     */
    @Async
    @Transactional
    public void generateDiffAsync(Long summaryId, String prompt) {
        executeWithRetry(
                summaryId,
                prompt,
                documentSummaryAiClient::diff,
                "DIFF"
        );
    }

    /* =========================================================
     * Core Retry Logic
     * ========================================================= */

    private void executeWithRetry(
            Long summaryId,
            String prompt,
            AiCall aiCall,
            String jobType
    ) {

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String content = aiCall.call(prompt);

                DocumentAISummary summary =
                        documentAiSummaryRepository.findById(summaryId)
                                .orElseThrow(() -> new RuntimeException("Summary not found"));

                summary.markCompleted(content);

                log.info(
                        "[AI {}] completed. summaryId={}, attempt={}",
                        jobType, summaryId, attempt
                );
                return;

            } catch (Exception e) {

                log.warn(
                        "[AI {}] attempt {}/{} failed. summaryId={}",
                        jobType, attempt, MAX_ATTEMPTS, summaryId, e
                );

                // 재시도 불가능한 예외면 즉시 실패 처리
                if (!isRetryable(e)) {
                    markFailed(summaryId, extractFailMessage(e));
                    return;
                }

                // 마지막 시도면 실패 확정
                if (attempt == MAX_ATTEMPTS) {
                    markFailed(summaryId, extractFailMessage(e));
                    return;
                }

                backoff(attempt);
            }
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long summaryId, String failMessage) {

        String safeMessage =
                failMessage != null && failMessage.length() > FAIL_MESSAGE_MAX_LENGTH
                        ? failMessage.substring(0, FAIL_MESSAGE_MAX_LENGTH)
                        : failMessage;
        documentAiSummaryRepository.findById(summaryId)
                .ifPresent(summary -> summary.markFailed(safeMessage));
    }


    private String extractFailMessage(Exception e) {

        if (e instanceof WebClientResponseException ex) {
            return ex.getResponseBodyAsString();
        }

        return Optional.ofNullable(e.getMessage())
                .orElse("AI 처리 중 알 수 없는 오류가 발생했습니다.");
    }

    private boolean isRetryable(Exception e) {
        return e instanceof WebClientResponseException
                || e instanceof TimeoutException;
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(BASE_BACKOFF_MILLIS * attempt);
        } catch (InterruptedException ignored) {
        }
    }


    @FunctionalInterface
    private interface AiCall {
        String call(String prompt);
    }
}