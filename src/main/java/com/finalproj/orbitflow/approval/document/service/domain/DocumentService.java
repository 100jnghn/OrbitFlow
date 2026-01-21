package com.finalproj.orbitflow.approval.document.service.domain;

import com.finalproj.orbitflow.approval.line.dto.ApprovalLineViewResDto;
import com.finalproj.orbitflow.approval.line.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.line.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.dto.*;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.schema.PdfContentSchema;
import com.finalproj.orbitflow.approval.document.service.render.DocumentContentRenderService;
import com.finalproj.orbitflow.approval.document.service.assembler.PdfContentSchemaAssembler;
import com.finalproj.orbitflow.approval.document.documentContent.entity.DocumentContent;
import com.finalproj.orbitflow.approval.document.documentContent.repository.DocumentContentRepository;
import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.document.file.enums.ReferenceType;
import com.finalproj.orbitflow.approval.document.file.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final ObjectMapper objectMapper;
    private final DocumentContentRepository documentContentRepository;
    private final ApprovalLineRepository approvalLineRepository;
    private final PdfContentSchemaAssembler pdfContentSchemaAssembler;
    private final DocumentContentRenderService documentContentRenderService;
    private final DocumentFileRepository documentFileRepository;


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

        // ⭐ 여기 추가
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
                // 기본적으로 조회 허용
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

        // 내 문서가 아니거나 반려가 아니면
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

    public DocumentPdfViewDto getPdfViewData(Long documentId) {

        // 1️⃣ 문서 조회
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (document.getStatus() != DocumentStatus.APPROVED) {
            throw new IllegalStateException("승인 완료된 문서만 PDF로 생성할 수 있습니다.");
        }

        // 2️⃣ 문서 본문(JSON) 조회
        DocumentContent content = documentContentRepository
                .findByDocument_Id(documentId)
                .orElseThrow(() -> new NotFoundException(
                        "DocumentContent not found. documentId=" + documentId
                ));

        // 3️⃣ JSON → FormTemplateSchema
        FormTemplateSchema schema = parseSchema(content.getContentJson());
        if (schema.getFields() == null || schema.getFields().isEmpty()) {
            throw new IllegalStateException("양식에 필드가 없습니다.");
        }

        // 4️⃣ FormTemplateSchema → PdfContentSchema
        PdfContentSchema pdfSchema =
                pdfContentSchemaAssembler.from(schema);

        // 🔥 5️⃣ PdfContentSchema → HTML (이미 만들어둔 렌더러 사용)
        String documentContentHtml =
                documentContentRenderService.render(documentId, pdfSchema);

        // 6️⃣ PDF View DTO 생성
        return DocumentPdfViewDto.builder()
                .documentId(document.getId())
                .title(document.getTitle())
                .status(document.getStatus())

                .submittedAt(
                        document.getSubmittedAt() == null ? null :
                                LocalDateTime.ofInstant(
                                        document.getSubmittedAt(),
                                        ZoneId.systemDefault()
                                )
                )
                .submittedBy(document.getWriter().getName())

                .approvedAt(
                        document.getUpdatedAt() == null ? null :
                                LocalDateTime.ofInstant(
                                        document.getUpdatedAt(),
                                        ZoneId.systemDefault()
                                )
                )

                // 🔥 서버에서 완성한 PDF 본문 HTML
                .documentContentHtml(documentContentHtml)
                .build();
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
        // 1. 현재 문서 조회 + 작성자 검증
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (!document.getWriter().getId().equals(employeeId)) {
            throw new InvalidRequestException("참조 문서는 작성자만 추가할 수 있습니다.");
        }

        // 2. 참조 대상 DocumentFile 조회
        DocumentFile target = documentFileRepository.findById(targetDocumentFileId)
                .orElseThrow(() -> new NotFoundException("참조 대상 파일을 찾을 수 없습니다."));

        // 3. 참조 대상 검증
        if (target.getReferenceType() != ReferenceType.DOCUMENT
                || target.getReferenceUrl() != null) {
            throw new InvalidRequestException("참조 가능한 문서가 아닙니다.");
        }

        Document targetDocument = target.getDocument();

        // 4. 현재 문서에 참조 DocumentFile 생성
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
        // 1. 문서 조회 + 작성자 검증
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (!document.getWriter().getId().equals(employeeId)) {
            throw new InvalidRequestException("참조 문서는 작성자만 제거할 수 있습니다.");
        }

        // 2. 참조 DocumentFile 조회
        DocumentFile reference = documentFileRepository.findById(documentFileId)
                .orElseThrow(() -> new NotFoundException("참조 문서를 찾을 수 없습니다."));

        // 3. 이 문서에 연결된 참조 문서인지 검증
        if (!reference.getDocument().getId().equals(documentId)) {
            throw new InvalidRequestException("해당 문서의 참조 문서가 아닙니다.");
        }

        // 4. 참조 문서인지 검증
        if (reference.getReferenceType() != ReferenceType.DOCUMENT) {
            throw new InvalidRequestException("참조 문서만 제거할 수 있습니다.");
        }

        // 5. 상태 검증 (작성 중 문서만)
        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new InvalidRequestException("작성 중인 문서에서만 참조 문서를 제거할 수 있습니다.");
        }

        // 6. 삭제
        documentFileRepository.delete(reference);
    }

}
