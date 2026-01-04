package com.finalproj.orbitflow.approval.documentAISummary.service;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.service.DocumentService;
import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryField;
import com.finalproj.orbitflow.approval.documentAISummary.dto.AiSummaryRequest;
import com.finalproj.orbitflow.approval.documentContent.service.DocumentContentService;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

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
public class DocumentAiSummaryService {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final DocumentContentService documentContentService;
    private final ObjectMapper objectMapper;

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

    /**
     * 문서 요약 요청 진입점
     */
    public void sendReqSummary(Long employeeId, Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // 열람 권한 검증 (작성자 / 결재자)
        documentService.validateViewPermission(employeeId, document);

        FormTemplateSchema schema =
                documentContentService.getDocumentContentByDocumentId(documentId);

        AiSummaryRequest request = buildAiSummaryRequest(schema);

        // 현재 단계에서는 로그로 확인
        try {
            log.info("AI Summary Request = {}",
                    objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.warn("Failed to serialize AI summary request", e);
        }

        // TODO
        // 1. AiClient 호출
        // 2. 요약 결과 저장
    }

    /**
     * FormTemplateSchema → AI 요약 입력 DTO 변환
     */
    private AiSummaryRequest buildAiSummaryRequest(FormTemplateSchema schema) {

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

        return new AiSummaryRequest(
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

        if ("event-date-range".equals(field.getFieldType())
                && field.getValue() instanceof Map<?, ?> raw) {

            Map<String, Object> map = (Map<String, Object>) raw;

            return Map.of(
                    "start", map.get("start"),
                    "end", map.get("end"),
                    "reason", map.get("reason")
            );
        }

        return field.getValue();
    }
}