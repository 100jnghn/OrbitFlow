package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.approvalLine.service.ApprovalLineDomainService;
import com.finalproj.orbitflow.approval.document.dto.DocumentCreateResDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

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
public class DocumentApplicationService {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final ApprovalLineDomainService  approvalLineDomainService;
    private final ObjectMapper objectMapper;


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
            beforeDocument = getDocument(employeeId, beforeDocumentId);
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

    private Document getDocument(Long employeeId, Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new NotFoundException("문서를 찾지 못했습니다. documentId: " + documentId));

        if (!document.getWriter().getId().equals(employeeId)) {
            throw new ForbiddenException("문서를 사용할 권한이 없습니다. documentId: " + documentId);
        }
        return document;
    }
}
