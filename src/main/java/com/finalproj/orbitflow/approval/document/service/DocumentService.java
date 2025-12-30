package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.approvalLine.dto.ApprovalLineViewResDto;
import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.dto.*;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

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
    private final EmployeeRepository employeeRepository;
    private final DocumentContentRepository documentContentRepository;
    private final ApprovalLineRepository approvalLineRepository;


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

        return documentRepository.findMyWrittenDocuments(companyId, employeeId, reqDto, pageable);
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

    @Transactional
    public void submitDocument(Long employeeId, Long documentId) {
        Document document = getDocument(employeeId, documentId);
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidRequestException("DRAFT 상태의 문서만 제출할 수 있습니다");
        }

        document.submit();

        List<ApprovalLine> lines = approvalLineRepository.findByDocument_IdOrderByOrderNoAsc(documentId);

        if(lines.isEmpty()) {
            throw new IllegalStateException("결재선이 존재하지 않습니다");
        }

        for (ApprovalLine line : lines) {
            line.markWaiting();
        }

        lines.get(0).markInProgress();

    }

    public DocumentDetailResDto getDocumentDetail(Long employeeId, Long documentId) {
        Document document = getDocument(employeeId, documentId);
        DocumentContent byDocumentId = documentContentRepository.findByDocument_Id(documentId)
                .orElseThrow(() -> new NotFoundException("Document with id: " + documentId + " not found"));

        FormTemplateSchema schema = parseSchema(byDocumentId.getContentJson());
        if (schema.getFields() == null || schema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }


        List<ApprovalLineViewResDto> lists = approvalLineRepository
                .findByDocument_IdOrderByOrderNoAsc(documentId)
                .stream()
                .map(ApprovalLineViewResDto::from)
                .toList();

        boolean myApprovalOrder = approvalLineRepository
                .findFirstByDocumentAndStatusOrderByOrderNoAsc(
                        document,
                        ApprovalStatus.IN_PROGRESS
                )
                .map(line -> line.getApprover().getId().equals(employeeId))
                .orElse(false);

        return DocumentDetailResDto.from(document, schema, lists, myApprovalOrder);
    }
}
