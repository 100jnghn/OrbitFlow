package com.finalproj.orbitflow.approval.documentAISummary.service;

import com.finalproj.orbitflow.approval.document.render.support.VacationTypeNameResolver;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.service.DocumentService;
import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.AiSummaryPromptBuilder;
import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryField;
import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryReqDto;
import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryResDto;
import com.finalproj.orbitflow.approval.documentAISummary.entity.DocumentAISummary;
import com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryStatus;
import com.finalproj.orbitflow.approval.documentAISummary.enums.SummaryType;
import com.finalproj.orbitflow.approval.documentAISummary.repository.DocumentAiSummaryRepository;
import com.finalproj.orbitflow.approval.documentContent.service.DocumentContentService;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentAiSummaryService
 * @since : 26. 1. 5. 월요일
 **/

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentAiSummaryService {

    private final DocumentAiSummaryRepository documentAiSummaryRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final DocumentContentService documentContentService;
    private final AiSummaryPromptBuilder aiSummaryPromptBuilder;
    private final DocumentAiSummaryAsyncService documentAiSummaryAsyncService;
    private final VacationTypeNameResolver vacationTypeNameResolver;


    /**
     * 요약에서 무조건 제외할 UI 전용 컴포넌트
     */
    private static final Set<String> EXCLUDE_TYPES = Set.of(
            "divider",
            "notice",
            "image"
    );
    /**
     * 핵심 의미를 판단하기 위한 라벨 키워드
     */
    private static final Set<String> CORE_LABEL_KEYWORDS = Set.of(
            "기간", "일정", "출장", "휴가", "근무",
            "사유", "목적",
            "금액", "비용", "예산"
    );
    @Value("${openai.model}")
    String openaiModel;

    /**
     * 문서 요약 요청 진입점
     */
    public void sendReqSummary(Long employeeId, Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // 열람 권한 검증 (작성자 / 결재자)
        documentService.validateViewPermission(employeeId, document);

        documentAiSummaryRepository
                .findByDocumentAndSummaryType(document, SummaryType.CONTENT)
                .filter(summary -> summary.getStatus() == SummaryStatus.COMPLETED)
                .ifPresent(summary -> {
                    throw new InvalidRequestException("이미 완료된 ai 요약이 존재합니다.");
                });


        FormTemplateSchema schema =
                documentContentService.getDocumentContentByDocumentId(documentId);

        AiSummaryReqDto request = buildAiSummaryRequest(schema);

        String prompt = aiSummaryPromptBuilder.build(request);

        DocumentAISummary summary = saveProcessingSummary(document, prompt);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        documentAiSummaryAsyncService.generateSummaryAsync(summary.getId(), prompt);
                    }
                }
        );

    }

    protected DocumentAISummary saveProcessingSummary(
            Document document,
            String prompt
    ) {
        return documentAiSummaryRepository.save(
                DocumentAISummary.builder()
                        .document(document)
                        .company(document.getCompany())
                        .summaryType(SummaryType.CONTENT)
                        .status(SummaryStatus.PROCESSING)
                        .prompt(prompt)
                        .content("")
                        .model(openaiModel)
                        .build()
        );
    }


    /**
     * FormTemplateSchema → AI 요약 입력 DTO 변환
     */
    private AiSummaryReqDto buildAiSummaryRequest(FormTemplateSchema schema) {

        List<AiSummaryField> coreFields = new ArrayList<>();
        List<AiSummaryField> optionalFields = new ArrayList<>();

        for (FormFieldSchema field : schema.getFields()) {

            if (EXCLUDE_TYPES.contains(field.getFieldType())) {
                continue;
            }

            if (!hasMeaningfulValue(field)) {
                continue;
            }

            AiSummaryField aiField = new AiSummaryField(
                    field.getLabel(),
                    field.getFieldType(),
                    simplifyValue(field)
            );

            if (isCoreField(field)) {
                coreFields.add(aiField);
            } else {
                optionalFields.add(aiField);
            }
        }

        String documentTitle = coreFields.stream()
                .filter(f -> "document-title".equals(f.getFieldType()))
                .map(f -> String.valueOf(f.getValue()))
                .findFirst()
                .orElse("문서");

        return new AiSummaryReqDto(
                documentTitle,
                coreFields,
                optionalFields
        );
    }

    /**
     * 값이 실제로 입력되었는지 판단
     */
    private boolean hasMeaningfulValue(FormFieldSchema field) {
        Object value = field.getValue();

        if (value == null) return false;

        if (value instanceof String s) {
            return !s.isBlank();
        }

        if (value instanceof Collection<?> c) {
            return !c.isEmpty();
        }

        if (value instanceof Map<?, ?> m) {
            return m.values().stream().anyMatch(v -> {
                if (v == null) return false;
                if (v instanceof String s) return !s.isBlank();
                if (v instanceof Collection<?> c) return !c.isEmpty();
                return true;
            });
        }

        return true;
    }

    /**
     * 핵심(core) 필드 판단
     */
    private boolean isCoreField(FormFieldSchema field) {

        // 타입 자체가 의미를 보장하는 경우
        if (Set.of("document-title", "event-date-range")
                .contains(field.getFieldType())) {
            return true;
        }

        // 라벨 키워드 기반 판단
        String label = field.getLabel();
        if (label == null) return false;

        return CORE_LABEL_KEYWORDS.stream()
                .anyMatch(label::contains);
    }

    /**
     * AI 전달용 value 단순화
     */
    @SuppressWarnings("unchecked")
    private Object simplifyValue(FormFieldSchema field) {

        if (!"event-date-range".equals(field.getFieldType())) {
            return field.getValue();
        }

        Map<String, Object> valueMap =
                (Map<String, Object>) field.getValue();

        Map<String, Object> metaMap =
                field.getMeta() != null
                        ? field.getMeta()
                        : Collections.emptyMap();

        String baseRole = metaMap.get("baseRole") instanceof String
                ? (String) metaMap.get("baseRole")
                : null;

        Map<String, Object> result = new HashMap<>();
        result.put("start", valueMap.get("start"));
        result.put("end", valueMap.get("end"));

        if ("VACATION".equals(baseRole)) {

            // 휴가 유형 변환
            if (valueMap.get("vacationTypeId") != null) {
                String vacationTypeName =
                        vacationTypeNameResolver.resolve(
                                String.valueOf(valueMap.get("vacationTypeId"))
                        );

                if (vacationTypeName != null) {
                    result.put("vacationType", vacationTypeName);
                }
            }

            // 휴가 사유
            if (valueMap.get("reason") != null) {
                result.put("reason", valueMap.get("reason"));
            }
        } else {
            if (valueMap.get("title") != null) {
                result.put("title", valueMap.get("title"));
            }
            if (valueMap.get("description") != null) {
                result.put("description", valueMap.get("description"));
            }
        }

        return result;
    }


    @Transactional(readOnly = true)
    public AiSummaryResDto readSummary(Long employeeId, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        documentService.validateViewPermission(employeeId, document);

        DocumentAISummary summary = documentAiSummaryRepository.findByDocumentAndSummaryType(document, SummaryType.CONTENT)
                .orElseThrow(() -> new NotFoundException("Not found Ai Summary"));

        return AiSummaryResDto.from(summary);
    }
}