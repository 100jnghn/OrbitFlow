package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.document.dto.*;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentService
 * @since : 25. 12. 22. 월요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final ObjectMapper objectMapper;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentContentRepository documentContentRepository;


    @Transactional(readOnly = true)
    public Page<DocumentListResDto> getMyWrittenDocuments(
            Long companyId,
            Long employeeId,
            int offset,
            int size,
            DocumentListReqDto reqDto
    ) {
        dateValidCheck(reqDto);

        Pageable pageable = PageRequest.of(offset, size);

        return documentRepository.findMyWrittenDocuments(
                companyId,
                employeeId,
                reqDto,
                pageable
        );
    }

    private static void dateValidCheck(DocumentListReqDto reqDto) {
        if (reqDto.getStartDate() != null && reqDto.getEndDate() != null) {
            if (reqDto.getStartDate().isAfter(reqDto.getEndDate())) {
                throw new InvalidRequestException("시작일은 종료일보다 클 수 없습니다.");
            }
        }
    }

    public Page<DocumentMyApprovalListResDto> getDocumentsToApprove(Long companyId, Long employeeId, int offset, int size, DocumentListReqDto reqDto) {
        dateValidCheck(reqDto);

        Pageable pageable = PageRequest.of(offset, size);

        return documentRepository.findMyApprovalDocuments(
                companyId,
                employeeId,
                reqDto,
                pageable
        );
    }

    @Transactional
    public DocumentCreateResDto createDraft(Long companyId, Long employeeId, Long formTemplateId, Long beforeDocumentId) {

        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("회사를 찾지 못했습니다."));

        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new NotFoundException("사원을 찾지 못했습니다."));

        FormTemplate formTemplate = formTemplateRepository.findByIdAndCompany_id(formTemplateId, companyId).orElseThrow(() -> new InvalidRequestException("사용할 수 없는 양식입니다."));

        FormTemplateSchema schema = parseSchema(formTemplate.getTemplateJson());

        if (schema.getFields() == null || schema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }

        String baseTitle = extractDefaultTitle(schema);
        String finalTitle = baseTitle + " (" + LocalDate.now() + ")";

        Document beforeDocument = null;
        if(beforeDocumentId != null) {
            beforeDocument = getDocument(employeeId, beforeDocumentId);
        }

        Document createdDocument = Document.builder()
                .company(company)
                .templateGroup(formTemplate.getTemplateGroup())
                .templateVersion(formTemplate.getVersion())
                .writer(employee)
                .title(finalTitle)
                .status(DocumentStatus.DRAFT)
                .beforeDocument(beforeDocument)
                .build();


        documentRepository.save(createdDocument);

        List<DocumentFormFieldDto> fields = schema.getFields().stream()
                .sorted(Comparator.comparing(FormFieldSchema::getOrder))
                .map(DocumentFormFieldDto::from)
                .toList();


        Map<String, Object> content = Map.of("fields", fields);
        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            throw new IllegalStateException("문서 내용 JSON 생성 실패", e);
        }

        DocumentContent documentContent = DocumentContent.builder()
                .document(createdDocument)
                .contentJson(contentJson)
                .build();

        documentContentRepository.save(documentContent);

        return DocumentCreateResDto.from(createdDocument.getId(), createdDocument.getStatus(), fields);
    }

    private Document getDocument(Long employeeId, Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new NotFoundException("문서를 찾지 못했습니다. documentId: " + documentId));

        if (!document.getWriter().getId().equals(employeeId)) {
            throw new ForbiddenException("문서를 사용할 권한이 없습니다. documentId: " + documentId);
        }
        return document;
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

    @Transactional
    public void updateDocument(Long employeeId, Long DocumentId, DocumentUpdateReqDto reqDto) {
        Document document = getDocument(employeeId, DocumentId);
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidRequestException("Draft 문서만 수정할 수 있습니다.");
        }

        if(reqDto.getTitle() != null) {
            document.updateTitle(reqDto.getTitle());
        }

        if(reqDto.getStatus() != null) {
            document.updateStatus(reqDto.getStatus());
        }
    }
}
