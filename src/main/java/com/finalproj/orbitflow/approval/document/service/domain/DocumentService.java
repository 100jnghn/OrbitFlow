package com.finalproj.orbitflow.approval.document.service.domain;

import com.finalproj.orbitflow.approval.document.content.entity.DocumentContent;
import com.finalproj.orbitflow.approval.document.content.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.document.dto.*;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.document.file.enums.ReferenceType;
import com.finalproj.orbitflow.approval.document.file.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import com.finalproj.orbitflow.approval.line.dto.ApprovalLineViewResDto;
import com.finalproj.orbitflow.approval.line.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.line.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 결재 문서 조회 및 관리 기능을 담당하는 도메인 서비스.
 * <p>
 * 문서 목록 조회, 상세 조회, 수정 가능 여부 판단 등
 * 결재 문서와 관련된 조회 중심 로직을 제공한다.
 * <p>
 * 문서 상태(DRAFT / IN_PROGRESS / APPROVED 등)와
 * 사용자 역할(작성자, 결재자)에 따라
 * 접근 가능 여부를 검증하는 책임을 함께 가진다.
 * <p>
 * 또한 문서 간 참조 검색 및 참조 문서 추가/제거 기능을 통해
 * 결재 문서 간 연결 관계를 관리한다.
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
    private final DocumentContentRepository documentContentRepository;
    private final ApprovalLineRepository approvalLineRepository;
    private final DocumentFileRepository documentFileRepository;

    private final ObjectMapper objectMapper;

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

    public Page<DocumentMyApprovalListResDto> getDocumentsToApprove(
            Long companyId,
            Long employeeId,
            int page,
            int size,
            DocumentListReqDto reqDto
    ) {
        dateValidCheck(reqDto);

        Pageable pageable = PageRequest.of(page, size);

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

    private Document getDocumentForRead(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new NotFoundException("문서를 찾지 못했습니다. documentId: " + documentId)
                );
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

        if (reqDto.getTitle() != null) {
            document.updateTitle(reqDto.getTitle());
        }

        if (reqDto.getStatus() != null) {
            document.updateStatus(reqDto.getStatus());
        }
    }


    public DocumentDetailResDto getDocumentDetail(Long employeeId, Long documentId) {

        Document document = getDocumentForRead(documentId);
        validateViewPermission(employeeId, document);

        DocumentContent byDocumentId = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow(() ->
                        new NotFoundException("Document with id: " + documentId + " not found")
                );

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

        Long pdfFileId = null;
        if (document.getStatus() == DocumentStatus.APPROVED) {
            pdfFileId = documentFileRepository
                    .findDocumentFileByDocumentAndType(
                            documentId,
                            ReferenceType.DOCUMENT,
                            DocumentFileStatus.FINAL
                    )
                    .map(df -> df.getFile().getId())
                    .orElse(null);
        }

        return DocumentDetailResDto.from(
                document,
                schema,
                lists,
                myApprovalOrder,
                pdfFileId
        );
    }


    public void validateViewPermission(
            Long employeeId,
            Document document
    ) {
        switch (document.getStatus()) {
            case DRAFT -> {
                if (!document.getWriter().getId().equals(employeeId)) {
                    throw new ForbiddenException("임시 문서는 작성자만 조회할 수 있습니다.");
                }
            }

            case IN_PROGRESS -> {
                boolean isWriter = document.getWriter().getId().equals(employeeId);
                boolean isApprover =
                        approvalLineRepository.existsByDocumentIdAndApproverId(
                                document.getId(), employeeId
                        );

                if (!isWriter && !isApprover) {
                    throw new ForbiddenException("결재 중인 문서는 결재자만 조회할 수 있습니다.");
                }
            }

            default -> {
                // APPROVED, REJECTED 등
            }
        }
    }

    public DocumentRevisionInfoResDto getDocumentRevision(
            Long employeeId,
            Long curDocId
    ) {
        Document current = documentRepository.findById(curDocId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        boolean isMine = current.getWriter().getId().equals(employeeId);

        Long beforeId = current.getBeforeDocument() != null
                ? current.getBeforeDocument().getId()
                : null;

        if (!isMine || current.getStatus() != DocumentStatus.REJECTED) {
            return DocumentRevisionInfoResDto.builder()
                    .beforeDocumentId(beforeId)
                    .nextDocumentId(null)
                    .nextDocumentStatus(null)
                    .mine(isMine)
                    .build();
        }

        return documentRepository
                .findTopByBeforeDocument_IdOrderByCreatedAtDesc(curDocId)
                .map(doc -> DocumentRevisionInfoResDto.builder()
                        .beforeDocumentId(beforeId)
                        .nextDocumentId(doc.getId())
                        .nextDocumentStatus(doc.getStatus())
                        .mine(true)
                        .build()
                )
                .orElse(
                        DocumentRevisionInfoResDto.builder()
                                .beforeDocumentId(beforeId)
                                .nextDocumentId(null)
                                .nextDocumentStatus(null)
                                .mine(true)
                                .build()
                );
    }



    public List<ReferenceSearchResDto> searchReference(
            Long employeeId,
            Long companyId,
            String keyword
    ) {
        return documentRepository.searchReference(
                employeeId,
                companyId,
                keyword,
                10
        );
    }


    @Transactional
    public void addReferenceDocument(
            Long employeeId,
            Long documentId,
            Long targetDocumentFileId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (!document.getWriter().getId().equals(employeeId)) {
            throw new InvalidRequestException("참조 문서는 작성자만 추가할 수 있습니다.");
        }

        DocumentFile target = documentFileRepository.findById(targetDocumentFileId)
                .orElseThrow(() -> new NotFoundException("참조 대상 파일을 찾을 수 없습니다."));

        if (target.getReferenceType() != ReferenceType.DOCUMENT
                || target.getReferenceUrl() != null) {
            throw new InvalidRequestException("참조 가능한 문서가 아닙니다.");
        }

        Document targetDocument = target.getDocument();

        DocumentFile reference = DocumentFile.builder()
                .document(document)
                .file(target.getFile())
                .referenceType(ReferenceType.DOCUMENT)
                .referenceTargetId(targetDocument.getId())
                .referenceUrl("/documents/files/" + target.getId())
                .status(DocumentFileStatus.TEMP)
                .build();

        documentFileRepository.save(reference);
    }

    @Transactional
    public void removeReferenceDocument(
            Long employeeId,
            Long documentId,
            Long documentFileId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (!document.getWriter().getId().equals(employeeId)) {
            throw new InvalidRequestException("참조 문서는 작성자만 제거할 수 있습니다.");
        }

        DocumentFile reference = documentFileRepository.findById(documentFileId)
                .orElseThrow(() -> new NotFoundException("참조 문서를 찾을 수 없습니다."));

        if (!reference.getDocument().getId().equals(documentId)) {
            throw new InvalidRequestException("해당 문서의 참조 문서가 아닙니다.");
        }

        if (reference.getReferenceType() != ReferenceType.DOCUMENT) {
            throw new InvalidRequestException("참조 문서만 제거할 수 있습니다.");
        }

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidRequestException("작성 중인 문서에서만 참조 문서를 제거할 수 있습니다.");
        }

        documentFileRepository.delete(reference);
    }
}
