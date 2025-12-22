package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.document.dto.DocumentCreateResDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentFormFieldDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentListReqDto;
import com.finalproj.orbitflow.approval.document.dto.DocumentListResDto;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormFieldSchema;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;

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


    @Transactional(readOnly = true)
    public Page<DocumentListResDto> getMyDocuments(
            Long companyId,
            Long employeeId,
            int offset,
            int size,
            DocumentListReqDto reqDto
    ) {
        if (reqDto.getStartDate() != null && reqDto.getEndDate() != null) {
            if (reqDto.getStartDate().isAfter(reqDto.getEndDate())) {
                throw new InvalidRequestException("시작일은 종료일보다 클 수 없습니다.");
            }
        }

        Pageable pageable = PageRequest.of(offset, size);

        return documentRepository.findMyDocuments(
                companyId,
                employeeId,
                reqDto,
                pageable
        );
    }

    //TODO 결재 문서 생성
    //preview 페이지와 동일하지만 수정가능한 형태로 사용자에게 제공 필요
    @Transactional
    public DocumentCreateResDto createDraft(Long companyId, Long formTemplateId, String documentTitle) {

        Company company = companyRepository.findById(companyId).orElseThrow(() -> new NotFoundException("회사를 찾지 못했습니다."));

        FormTemplate formTemplate = formTemplateRepository.findByIdAndCompany_id(formTemplateId, companyId).orElseThrow(() -> new InvalidRequestException("사용할 수 없는 양식입니다."));



        Document createdDocument = Document.builder()
                .company(company)
                .templateGroup(formTemplate.getTemplateGroup())
                .templateVersion(formTemplate.getVersion())
                .title(documentTitle)
                .status(DocumentStatus.DRAFT)
                //.beforeDocument() TODO 추후 수정에서 구현
                .build();


        documentRepository.save(createdDocument);

        FormTemplateSchema schema = parseSchema(formTemplate.getTemplateJson());

        if (schema.getFields() == null || schema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }


        List<DocumentFormFieldDto> fields = schema.getFields().stream()
                .sorted(Comparator.comparing(FormFieldSchema::getOrder))
                .map(DocumentFormFieldDto::from)
                .toList();


        return DocumentCreateResDto.from(createdDocument.getId(), createdDocument.getTitle(), createdDocument.getStatus(), fields);
    }

    private FormTemplateSchema parseSchema(String templateJson) {
        try {
            return objectMapper.readValue(templateJson, FormTemplateSchema.class);
        } catch (Exception e) {
            throw new IllegalStateException("template_json 파싱 실패", e);
        }
    }

}
