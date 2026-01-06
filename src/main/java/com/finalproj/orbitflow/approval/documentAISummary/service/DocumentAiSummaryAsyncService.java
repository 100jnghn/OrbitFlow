package com.finalproj.orbitflow.approval.documentAISummary.service;

import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common.AiClient;
import com.finalproj.orbitflow.approval.documentAISummary.entity.DocumentAISummary;
import com.finalproj.orbitflow.approval.documentAISummary.repository.DocumentAiSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final DocumentAiSummaryRepository documentAiSummaryRepository;
    private final AiClient aiClient;

    /**
     * 비동기 요약 생성
     * - 기존 PROCESSING row를 조회해서 상태를 전이
     */
    @Async
    @Transactional
    public void generateSummaryAsync(Long summaryId, String prompt) {

        DocumentAISummary summaryEntity =
                documentAiSummaryRepository.findById(summaryId)
                        .orElseThrow(() -> new RuntimeException("Summary not found"));

        try {
            String content = aiClient.summarize(prompt);

            summaryEntity.markCompleted(content);

        } catch (Exception e) {
            log.error("AI summary failed. summaryId={}", summaryId, e);

            summaryEntity.markFailed();
        }
    }

    @Async
    @Transactional
    public void generateDiffAsync(Long summaryId, String prompt) {

        DocumentAISummary summaryEntity =
                documentAiSummaryRepository.findById(summaryId)
                        .orElseThrow(() -> new RuntimeException("Summary not found"));

        try {
            String content = aiClient.diff(prompt);

            summaryEntity.markCompleted(content);

        } catch (Exception e) {
            log.error("AI diff failed. summaryId={}", summaryId, e);
            summaryEntity.markFailed();
        }
    }

}
