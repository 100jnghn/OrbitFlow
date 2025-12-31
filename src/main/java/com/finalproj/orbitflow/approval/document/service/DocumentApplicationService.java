package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.approvalLine.service.ApprovalLineDomainService;
import com.finalproj.orbitflow.approval.document.dto.DocumentCreateResDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentApplicationService
 * @since : 25. 12. 28. 일요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DocumentApplicationService {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final ApprovalLineDomainService approvalLineDomainService;
    private final ObjectMapper objectMapper;
    private final ApprovalLineRepository approvalLineRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Transactional
    public DocumentCreateResDto createDraft(
            Long companyId,
            Long employeeId,
            Long formTemplateId,
            Long beforeDocumentId
    ) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("회사를 찾지 못했습니다."));

        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new NotFoundException("사원을 찾지 못했습니다."));

        FormTemplate formTemplate = formTemplateRepository.findByIdAndCompany_id(formTemplateId, companyId).orElseThrow(() -> new InvalidRequestException("사용할 수 없는 양식입니다."));

        // ✅ 1. 활성화 여부
        if (!formTemplate.isActive()) {
            throw new InvalidRequestException("아직 활성화되지 않은 양식입니다.");
        }

        // ✅ 2. 결재 규칙 존재 여부
        if (formTemplate.getApprovalRuleJson() == null
                || formTemplate.getApprovalRuleJson().isBlank()) {
            throw new InvalidRequestException("결재 규칙이 설정되지 않은 양식입니다.");
        }

        // ✅ 3. 결재 규칙 사전 검증 (강력 추천)
        try {
            approvalLineDomainService.validateApprovalRule(formTemplate);
        } catch (Exception e) {
            throw new InvalidRequestException(
                    "결재 규칙이 올바르지 않은 양식입니다." + e
            );
        }

        FormTemplateSchema schema = parseSchema(formTemplate.getTemplateJson());

        if (schema.getFields() == null || schema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }

        String baseTitle = extractDefaultTitle(schema);
        String finalTitle = baseTitle + " (" + LocalDate.now() + ")";

        Document beforeDocument = null;
        if (beforeDocumentId != null) {
            beforeDocument = getDocumentForWriter(employeeId, beforeDocumentId);
        }

        Document createdDocument = Document.createDraft(
                company,
                employee,
                formTemplate,
                finalTitle,
                beforeDocument
        );

        documentRepository.save(createdDocument);


        DocumentContent documentContent = DocumentContent.fromSchema(createdDocument, schema, objectMapper);

        documentContentRepository.save(documentContent);

        approvalLineDomainService.initializeDraftLines(
                createdDocument,
                formTemplate,
                employee
        );

        return DocumentCreateResDto.from(createdDocument.getId());
    }

    @Transactional
    public void approve(Long employeeId, Long documentId, String comment) {

        // 1. 문서 조회 + 상태 검증
        Document document = getDocumentForApprover(employeeId, documentId);

        if (document.getStatus() != DocumentStatus.IN_PROGRESS) {
            throw new InvalidRequestException("진행중인 결재 문서만 승인할 수 있습니다.");
        }

        // 2. 결재선 조회
        List<ApprovalLine> lines = getApprovalLines(documentId);

        log.info("[APPROVE/REJECT] employeeId={}, documentId={}", employeeId, documentId);

        lines.forEach(l ->
                log.info(
                        "[LINE] order={}, approverId={}, status={}",
                        l.getOrderNo(),
                        l.getApprover().getId(),
                        l.getStatus()
                )
        );


        // 3. 내 결재선 찾기
        ApprovalLine myLine = lines.stream()
                .filter(line ->
                        line.getApprover().getId().equals(employeeId)
                                && line.getStatus() == ApprovalStatus.IN_PROGRESS
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidRequestException("자신의 결재 차례가 아닙니다.")
                );


        if (myLine.getStatus() != ApprovalStatus.IN_PROGRESS) {
            throw new InvalidRequestException("자신의 결재 차례에만 승인할 수 있습니다.");
        }

        // 4. 내 결재선 승인
        myLine.markApproved(comment); // → APPROVED

        // 5. 다음 결재선 찾기
        ApprovalLine nextLine = lines.stream()
                .filter(line ->
                        line.getOrderNo() > myLine.getOrderNo()
                                && line.getStatus() == ApprovalStatus.WAITING
                )
                .findFirst()
                .orElse(null);

        if (nextLine != null) {
            // 6-1. 다음 결재자가 있다 → 결재 계속
            nextLine.markInProgress(); // WAITING → IN_PROGRESS
        } else {
            // 6-2. 다음 결재자가 없다 → 최종 승인
            document.approve();

            applicationEventPublisher.publishEvent(document.getId());
        }
    }


    @Transactional
    public void reject(Long employeeId, Long documentId, String comment) {

        // 1. 문서 조회 + 상태 검증
        Document document = getDocumentForApprover(employeeId, documentId);

        if (document.getStatus() != DocumentStatus.IN_PROGRESS) {
            throw new InvalidRequestException("진행중인 결재 문서만 반려할 수 있습니다.");
        }

        // 2. 결재선 조회
        List<ApprovalLine> lines = getApprovalLines(documentId);

        log.info("[APPROVE/REJECT] employeeId={}, documentId={}", employeeId, documentId);

        lines.forEach(l ->
                log.info(
                        "[LINE] order={}, approverId={}, status={}",
                        l.getOrderNo(),
                        l.getApprover().getId(),
                        l.getStatus()
                )
        );


        // 3. 내 결재선 찾기
        ApprovalLine myLine = lines.stream()
                .filter(line ->
                        line.getApprover().getId().equals(employeeId)
                                && line.getStatus() == ApprovalStatus.IN_PROGRESS
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidRequestException("자신의 결재 차례가 아닙니다.")
                );


        if (myLine.getStatus() != ApprovalStatus.IN_PROGRESS) {
            throw new InvalidRequestException("자신의 결재 차례에만 반려할 수 있습니다.");
        }

        // 4. 내 결재선 반려
        myLine.reject(comment); // → REJECTED

        // 5. 내 이후 결재자 CANCELLED 처리
        lines.stream()
                .filter(line ->
                        line.getOrderNo() > myLine.getOrderNo()
                                && line.getStatus() == ApprovalStatus.WAITING
                )
                .forEach(ApprovalLine::markCancelled); // → CANCELLED

        // 6. 문서 상태 반려
        document.reject();
    }

    private List<ApprovalLine> getApprovalLines(Long documentId) {
        List<ApprovalLine> lines =
                approvalLineRepository.findByDocument_IdOrderByOrderNoAsc(documentId);

        if (lines.isEmpty()) {
            throw new NotFoundException("문서의 결재선을 찾을 수 없습니다.");
        }
        return lines;
    }


    /* *
     *
     *
     * */

    private FormTemplateSchema parseSchema(String templateJson) {
        try {
            return objectMapper.readValue(templateJson, FormTemplateSchema.class);
        } catch (Exception e) {
            throw new IllegalStateException("template_json 파싱 실패", e);
        }
    }

    private String extractDefaultTitle(FormTemplateSchema schema) {

        return schema.getFields().stream()
                .filter(f -> "document-title".equals(f.getFieldId()))
                .findFirst()
                .map(f -> {
                    Object value = f.getMeta().get("value");
                    if (value == null || value.toString().isBlank()) {
                        return "제목 없음"; // fallback
                    }
                    return value.toString();
                })
                .orElse("제목 없음");
    }

    private Document getDocumentForWriter(Long employeeId, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾지 못했습니다."));

        if (!document.getWriter().getId().equals(employeeId)) {
            throw new ForbiddenException("작성자만 접근할 수 있습니다.");
        }
        return document;
    }

    private Document getDocumentForApprover(Long employeeId, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾지 못했습니다."));

        return document;
    }


}
