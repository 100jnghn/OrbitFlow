package com.finalproj.orbitflow.approval.document.service.application;

import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.approvalLine.service.ApprovalLineDomainService;
import com.finalproj.orbitflow.approval.document.dto.DocumentCreateResDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.schema.FormTemplateSchemaParser;
import com.finalproj.orbitflow.approval.document.service.assembler.DocumentContentAssembler;
import com.finalproj.orbitflow.approval.document.support.access.DocumentAccessValidator;
import com.finalproj.orbitflow.approval.document.support.title.DocumentTitleResolver;
import com.finalproj.orbitflow.approval.document.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.document.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.document.file.service.DocumentFileCleanupService;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 결재 문서 초안(DRAFT) 생성 유즈케이스를 담당하는 Application Service.
 * - 양식 유효성 검증
 * - 결재 규칙 사전 검증
 * - 문서 초안 및 본문 생성
 * - 결재선 초안 초기화
 *
 * @author : Choi MinHyeok
 * @filename : DocumentDraftApplicationService
 * @since : 26. 1. 21. 수요일
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentDraftApplicationService {

    private final DocumentRepository documentRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final DocumentContentRepository documentContentRepository;
    private final DocumentFileRepository documentFileRepository;
    private final ApprovalLineRepository approvalLineRepository;


    private final ApprovalLineDomainService approvalLineDomainService;
    private final FormTemplateSchemaParser formTemplateSchemaParser;
    private final DocumentAccessValidator documentAccessValidator;
    private final DocumentTitleResolver documentTitleResolver;
    private final DocumentContentAssembler documentContentAssembler;
    private final DocumentFileCleanupService documentFileCleanupService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public DocumentCreateResDto createDraft(
            Long companyId,
            Long employeeId,
            Long formTemplateId,
            Long beforeDocumentId
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("회사를 찾지 못했습니다."));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("사원을 찾지 못했습니다."));

        FormTemplate formTemplate =
                formTemplateRepository.findById(formTemplateId)
                        .orElseThrow(() -> new NotFoundException("양식을 찾지 못했습니다."));


        if (!formTemplate.getCompany().getId().equals(company.getId())) {
            throw new InvalidRequestException("사용할 수 없는 양식입니다.");
        }

        // 양식 활성 상태 검증
        if (!formTemplate.getTemplateGroup().getActive()) {
            throw new InvalidRequestException("해당 양식은 현재 사용 불가 상태입니다.");
        }

        if (!formTemplate.isActive()) {
            throw new InvalidRequestException("활성화되지 않은 양식입니다.");
        }

        // 결재 규칙 존재 여부 검증
        if (formTemplate.getApprovalRuleJson() == null
                || formTemplate.getApprovalRuleJson().isBlank()) {
            throw new InvalidRequestException("결재 규칙이 설정되지 않은 양식입니다.");
        }

        // 결재 규칙 사전 검증
        try {
            approvalLineDomainService.validateApprovalRule(formTemplate);
        } catch (Exception e) {
            throw new InvalidRequestException("결재 규칙이 올바르지 않은 양식입니다.");
        }

        // 양식 스키마 파싱
        FormTemplateSchema templateSchema =
                formTemplateSchemaParser.parse(formTemplate.getTemplateJson());

        if (templateSchema.getFields() == null || templateSchema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }

        // 문서 제목 결정
        String resolvedTitle = documentTitleResolver.resolve(templateSchema);
        String draftTitle = resolvedTitle + " (" + LocalDate.now() + ")";

        // 재기안 원본 문서 조회
        Document sourceDocument = null;
        if (beforeDocumentId != null) {
            sourceDocument =
                    documentAccessValidator.getForWriter(employee, beforeDocumentId);
        }

        // 문서 초안 생성
        Document draftDocument = Document.createDraft(
                company,
                employee,
                formTemplate,
                draftTitle,
                sourceDocument
        );

        documentRepository.save(draftDocument);

        // 문서 본문 생성
        DocumentContent documentContent =
                documentContentAssembler.fromSchema(draftDocument, templateSchema);

        documentContentRepository.save(documentContent);

        // 결재선 초안 초기화
        approvalLineDomainService.initializeDraftLines(
                draftDocument,
                formTemplate,
                employee
        );

        return DocumentCreateResDto.from(draftDocument.getId());
    }

    @Transactional
    public void deleteDraft(Long employeeId, Long documentId) {

        // 문서 조회 + 기본 검증
        Document draft = validateDeletableDraft(employeeId, documentId);

        // 첨부파일 관계 삭제
        List<DocumentFile> documentFiles =
                documentFileRepository.findByDocument_Id(draft.getId());

        documentFileRepository.deleteAll(documentFiles);
        entityManager.flush();

        documentFileCleanupService.cleanupDetachedFiles(documentFiles);

        // 결재선 삭제
        approvalLineRepository.deleteByDocument(draft);

        // 문서 본문 삭제
        documentContentRepository.deleteByDocument(draft);

        // 문서 삭제
        documentRepository.delete(draft);
    }

    private Document validateDeletableDraft(Long employeeId, Long documentId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서 탐색 실패"));

        // 상태 검증
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidRequestException("임시 문서만 삭제할 수 있습니다.");
        }

        // 작성자 검증
        if (!document.getWriter().getId().equals(employeeId)) {
            throw new InvalidRequestException("삭제 권한 없음");
        }

        return document;
    }

}