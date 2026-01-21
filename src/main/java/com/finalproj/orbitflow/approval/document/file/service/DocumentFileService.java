package com.finalproj.orbitflow.approval.document.file.service;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.service.domain.DocumentService;
import com.finalproj.orbitflow.approval.document.file.dto.DocumentFileAttachedListResDto;
import com.finalproj.orbitflow.approval.document.file.dto.DocumentFileUploadResDto;
import com.finalproj.orbitflow.approval.document.file.dto.PdfStatusRes;
import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.document.file.enums.PdfStatus;
import com.finalproj.orbitflow.approval.document.file.enums.ReferenceType;
import com.finalproj.orbitflow.approval.document.file.repository.DocumentFileRepository;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 결재 문서에 첨부되는 파일 및 이미지, PDF 문서를 관리하는 서비스.
 * <p>
 * 문서 작성 과정에서 업로드되는 일반 첨부파일과 본문 이미지 파일을 처리하며,
 * 문서 승인 이후 생성되는 최종 PDF 파일의 매핑 및 조회 상태도 함께 관리한다.
 * <p>
 * 파일 업로드 및 조회 시에는 문서의 상태(DRAFT / APPROVED)와
 * 사용자 권한(작성자, 결재자)을 기준으로 접근 가능 여부를 검증한다.
 * <p>
 * 실제 파일 저장과 스트리밍은 FileService가 담당하며,
 * 이 서비스는 문서(Document)와 파일(File) 간의 관계 및
 * 문서 흐름에 따른 파일 상태 관리를 책임진다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFileService
 * @since : 26. 1. 2. 금요일
 */


@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentFileService {

    private final DocumentFileRepository documentFileRepository;
    private final DocumentRepository documentRepository;
    private final FileService fileService;
    private final DocumentService documentService;


    @Transactional
    public DocumentFileUploadResDto uploadDocumentFile(Long companyId, Long uploaderId, Long documentId, MultipartFile multipartFile) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document Not Found"));


        if (!document.getWriter().getId().equals(uploaderId)) {
            throw new InvalidRequestException("첨부 파일 업로드 권한 없음");
        }

        File file = fileService.upload(
                companyId,
                FileDomain.DOCUMENT,
                multipartFile
        );

        DocumentFile documentFile = DocumentFile.builder()
                .document(document)
                .file(file)
                .referenceType(ReferenceType.ATTACHMENT)
                .status(DocumentFileStatus.TEMP)
                .fieldId(null)
                .build();

        documentFileRepository.save(documentFile);


        return new DocumentFileUploadResDto(
                documentFile.getId(),
                file.getId(),
                file.getOriginFile(),
                file.getFileSize()
        );
    }

    @Transactional
    public DocumentFileUploadResDto uploadImageFile(Long companyId, Long uploaderId, Long documentId, String fieldId, MultipartFile multipartFile) {
        if (fieldId == null || fieldId.isBlank()) {
            throw new InvalidRequestException("이미지 업로드에는 fieldId가 필요합니다.");
        }

        String contentType = multipartFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidRequestException("이미지 파일만 업로드할 수 있습니다.");
        }


        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document Not Found"));


        if (!document.getWriter().getId().equals(uploaderId)) {
            throw new InvalidRequestException("첨부 파일 업로드 권한 없음");
        }

        File file = fileService.upload(
                companyId,
                FileDomain.DOCUMENT,
                multipartFile
        );

        DocumentFile documentFile = DocumentFile.builder()
                .document(document)
                .file(file)
                .referenceType(ReferenceType.IMAGE)
                .status(DocumentFileStatus.TEMP)
                .fieldId(fieldId)
                .build();

        documentFileRepository.save(documentFile);


        return new DocumentFileUploadResDto(
                documentFile.getId(),
                file.getId(),
                file.getOriginFile(),
                file.getFileSize()
        );
    }

    public List<DocumentFileAttachedListResDto> getAttachedFiles(
            Long employeeId,
            Long documentId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document Not Found"));

        documentService.validateViewPermission(employeeId, document);

        return documentFileRepository
                .findAttachedFilesByDocumentId(documentId)
                .stream()
                .filter(p ->
                        !(p.getReferenceType() == ReferenceType.DOCUMENT
                                && p.getReferenceTargetId() == null)
                )
                .map(p -> new DocumentFileAttachedListResDto(
                        p.getDocumentFileId(),
                        p.getFileId(),
                        p.getDisplayName(),
                        p.getWriterName(),
                        p.getFileSize(),
                        p.getReferenceTargetId(),
                        p.getStatus(),
                        p.getFieldId()
                ))
                .toList();
    }


    @Transactional
    public void updateStatus(Long employeeId, Long documentFileId, DocumentFileStatus status) {
        DocumentFile file =  documentFileRepository.findById(documentFileId)
                .orElseThrow(() -> new NotFoundException("Document Not Found"));

        if (!file.getCreatedBy().equals(employeeId)) {
            throw new InvalidRequestException("자신이 작성한 문서의 첨부파일의 상태만 변경할 수 있습니다.");
        }

        if (file.getStatus().equals(DocumentFileStatus.FINAL)) {
            throw new InvalidRequestException("상신한 문서의 첨부파일은 수정할 수 없습니다.");
        }

        file.updateStatus(status);
    }


    public ResponseEntity<byte[]> getDocumentImage(
            Long employeeId,
            Long documentId,
            Long fileId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        documentService.validateViewPermission(employeeId, document);

        DocumentFile documentFile = documentFileRepository
                .findByDocument_IdAndFile_Id(documentId, fileId)
                .orElseThrow(() -> new ForbiddenException("문서에 포함되지 않은 파일입니다."));

        if (document.getStatus() == DocumentStatus.DRAFT) {
            // 작성자는 TEMP 이미지도 조회 가능
            if (!document.getWriter().getId().equals(employeeId)) {
                throw new ForbiddenException("권한 없음");
            }
        } else {
            // 결재 이후는 FINAL만
            if (documentFile.getStatus() != DocumentFileStatus.FINAL) {
                throw new ForbiddenException("열람할 수 없는 파일 상태입니다.");
            }
        }

        return fileService.streamImage(documentFile.getFile());
    }

    public Resource loadImage(Long documentFileId) {

        DocumentFile documentFile = documentFileRepository
                .findById(documentFileId)
                .orElseThrow(() -> new NotFoundException("문서 파일 없음"));

        if (!documentFile.isImage()) {
            throw new IllegalStateException("이미지가 아님");
        }

        return fileService.loadAsResource(documentFile.getFile());
    }

    @Transactional
    public void mappingPdf(Document document, File pdfFile) {

        documentFileRepository
                .findDocumentFileByDocumentAndType(
                        document.getId(),
                        ReferenceType.DOCUMENT,
                        DocumentFileStatus.FINAL
                )
                .ifPresent(df -> df.updateStatus(DocumentFileStatus.DELETED));

        DocumentFile documentFile = DocumentFile.builder()
                .document(document)
                .file(pdfFile)
                .referenceType(ReferenceType.DOCUMENT)
                .referenceTargetId(null)
                .status(DocumentFileStatus.FINAL)
                .build();

        documentFileRepository.save(documentFile);
    }


    public PdfStatusRes getPdfStatus(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        if (document.getStatus() != DocumentStatus.APPROVED) {
            return new PdfStatusRes(PdfStatus.NONE, null);
        }

        Optional<DocumentFile> pdfOpt =
                documentFileRepository
                        .findByDocument_IdAndReferenceTypeAndReferenceTargetIdIsNullAndStatus(
                                documentId,
                                ReferenceType.DOCUMENT,
                                DocumentFileStatus.FINAL
                        );

        return pdfOpt.map(documentFile -> new PdfStatusRes(
                PdfStatus.READY,
                documentFile.getFile().getId()
        )).orElseGet(() -> new PdfStatusRes(PdfStatus.GENERATING, null));
    }
}
