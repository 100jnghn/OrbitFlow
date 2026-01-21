package com.finalproj.orbitflow.approval.document.ai.service;

import com.finalproj.orbitflow.approval.line.service.ApprovalLineService;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.render.support.VacationTypeNameResolver;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.service.domain.DocumentService;
import com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff.AiDiffPromptBuilder;
import com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary.AiSummaryPromptBuilder;
import com.finalproj.orbitflow.approval.document.ai.dto.*;
import com.finalproj.orbitflow.approval.document.ai.entity.DocumentAISummary;
import com.finalproj.orbitflow.approval.document.ai.enums.AiStatus;
import com.finalproj.orbitflow.approval.document.ai.enums.SummaryType;
import com.finalproj.orbitflow.approval.document.ai.repository.DocumentAiSummaryRepository;
import com.finalproj.orbitflow.approval.document.content.service.DocumentContentService;
import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.document.file.enums.ReferenceType;
import com.finalproj.orbitflow.approval.document.file.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.form.template.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
    private final AiDiffPromptBuilder aiDiffPromptBuilder;
    private final DocumentAiSummaryAsyncService documentAiSummaryAsyncService;
    private final VacationTypeNameResolver vacationTypeNameResolver;
    private final DocumentFileRepository documentFileRepository;


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
    private final ApprovalLineService approvalLineService;
    @Value("${openai.model}")
    String openaiModel;

    /**
     * 문서 요약 요청 진입점
     */
    public void sendReqSummary(Long employeeId, Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        // 열람 권한 검증 (작성자 / 결재자)
        documentService.validateViewPermission(employeeId, document);

        documentAiSummaryRepository
                .findByDocumentAndSummaryType(document, SummaryType.CONTENT)
                .filter(summary ->
                        summary.getStatus() == AiStatus.COMPLETED
                                || summary.getStatus() == AiStatus.PROCESSING
                )
                .ifPresent(summary -> {
                    throw new InvalidRequestException("이미 완료된 ai 요약이 존재합니다.");
                });


        FormTemplateSchema schema =
                documentContentService.getDocumentContentByDocumentId(documentId);

        List<String> attachmentFiles = getAttachmentFiles(documentId);


        AiSummaryReqDto request = buildAiSummaryRequest(schema, attachmentFiles);

        String prompt = aiSummaryPromptBuilder.build(request, schema);

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

    @NotNull
    private List<String> getAttachmentFiles(Long documentId) {
        return documentFileRepository.findByDocument_Id(documentId)
                .stream()
                .filter(df -> df.getReferenceType() == ReferenceType.ATTACHMENT)
                .filter(df -> df.getStatus() == DocumentFileStatus.FINAL)
                .map(DocumentFile::getFile)
                .filter(Objects::nonNull)
                .map(File::getOriginFile)
                .toList();
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
                        .status(AiStatus.PROCESSING)
                        .prompt(prompt)
                        .content("")
                        .model(openaiModel)
                        .build()
        );
    }


    /**
     * FormTemplateSchema → AI 요약 입력 DTO 변환
     */
    private AiSummaryReqDto buildAiSummaryRequest(FormTemplateSchema schema, List<String> list) {

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
                    simplifyValue(field),
                    field.getMeta()
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
                optionalFields,
                list
        );
    }

    /**
     * 값이 실제로 입력되었는지 판단
     */
    public static boolean hasMeaningfulValue(FormFieldSchema field) {
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

        Map<String, Object> result = new LinkedHashMap<>();
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
    public AiSummaryResDto readSummary(Long employeeId, Long documentId, SummaryType summaryType) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        documentService.validateViewPermission(employeeId, document);

        Optional<DocumentAISummary> summary = documentAiSummaryRepository.findByDocumentAndSummaryType(document, summaryType);

        return summary.map(AiSummaryResDto::from).orElse(null);
    }

    public void sendReqDiff(Long employeeId, Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        Document beforeDocument = document.getBeforeDocument();
        if (beforeDocument == null) {
            throw new InvalidRequestException("비교 대상 문서가 존재하지 않습니다.");
        }

        // 열람 권한 검증
        documentService.validateViewPermission(employeeId, document);

        // 이미 완료된 비교 요청 방지
        documentAiSummaryRepository
                .findByDocumentAndBeforeDocumentAndSummaryType(
                        document,
                        beforeDocument,
                        SummaryType.DIFF
                )
                .filter(summary ->
                        summary.getStatus() == AiStatus.COMPLETED
                                || summary.getStatus() == AiStatus.PROCESSING
                )
                .ifPresent(summary -> {
                    throw new InvalidRequestException("이미 완료된 AI 비교가 존재합니다.");
                });

        // 스키마 조회 (동일 양식 전제 → current 기준)
        FormTemplateSchema schema =
                documentContentService.getDocumentContentByDocumentId(documentId);

        // 첨부파일명 조회
        List<String> beforeAttachedNames = getAttachmentFiles(beforeDocument.getId());

        List<String> currentAttachedNames = getAttachmentFiles(document.getId());

        // 동일 규칙으로 정규화
        AiSummaryReqDto before =
                buildAiSummaryRequest(
                        documentContentService.getDocumentContentByDocumentId(beforeDocument.getId()),
                        beforeAttachedNames
                );

        AiSummaryReqDto current =
                buildAiSummaryRequest(
                        schema,
                        currentAttachedNames
                );

        // Diff 요청 DTO
        AiDiffReqDto diffRequest = new AiDiffReqDto(before, current);

        String rejectComment =
                approvalLineService.findRejectComment(beforeDocument.getId());

        String prompt = aiDiffPromptBuilder.build(
                new DiffPromptContext(
                        diffRequest,
                        schema,
                        rejectComment
                )
        );

        // PROCESSING 상태 저장
        DocumentAISummary summary = saveProcessingDiff(document, prompt);

        // 커밋 이후 비동기 실행
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        documentAiSummaryAsyncService
                                .generateDiffAsync(summary.getId(), prompt);
                    }
                }
        );
    }


    protected DocumentAISummary saveProcessingDiff(
            Document document,
            String prompt
    ) {
        return documentAiSummaryRepository.save(
                DocumentAISummary.builder()
                        .document(document)
                        .beforeDocument(document.getBeforeDocument())
                        .company(document.getCompany())
                        .summaryType(SummaryType.DIFF)
                        .status(AiStatus.PROCESSING)
                        .prompt(prompt)
                        .content("")
                        .model(openaiModel)
                        .build()
        );
    }


}