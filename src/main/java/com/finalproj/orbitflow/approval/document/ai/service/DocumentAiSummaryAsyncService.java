package com.finalproj.orbitflow.approval.document.ai.service;

import com.finalproj.orbitflow.approval.aiClient.DocumentSummaryAiClient;
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
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentAiSummaryAsyncService
 * @since : 26. 1. 5. 월요일
 **/


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

    /* =========================================================
     * Failure Handling
     * ========================================================= */

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

    /* =========================================================
     * Functional Interface
     * ========================================================= */

    @FunctionalInterface
    private interface AiCall {
        String call(String prompt);
    }
}