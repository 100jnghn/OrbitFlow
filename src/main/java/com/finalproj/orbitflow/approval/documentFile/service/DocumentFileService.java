package com.finalproj.orbitflow.approval.documentFile.service;

import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.service.DocumentService;
import com.finalproj.orbitflow.approval.documentFile.dto.DocumentFileAttachedListResDto;
import com.finalproj.orbitflow.approval.documentFile.dto.DocumentFileUploadResDto;
import com.finalproj.orbitflow.approval.documentFile.entity.DocumentFile;
import com.finalproj.orbitflow.approval.documentFile.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.documentFile.enums.ReferenceType;
import com.finalproj.orbitflow.approval.documentFile.repository.DocumentFileRepository;
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
import java.util.Objects;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFileService
 * @since : 26. 1. 2. 금요일
 **/

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentFileService {

    private final DocumentFileRepository documentFileRepository;
    private final DocumentRepository documentRepository;
    private final FileService fileService;
    private final DocumentService documentService;
    private final ApprovalLineRepository approvalLineRepository;


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

        if (!Objects.requireNonNull(multipartFile.getContentType()).startsWith("image/")) {
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

    public List<DocumentFileAttachedListResDto> getAttachedFiles(Long employeeId, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document Not Found"));
        /*
        * 권한 검증.
        * case1. DRAFT이면 본인만
        * case2. IN_PROGRESS 이상이라면 결재선 포함된 사람 모두
        * */
        documentService.validateViewPermission(employeeId, document);
        //검증 통과(본인 + 결재자만이 첨부파일 목록을 확인할 수 있도록 현재 설계
        return documentFileRepository
                .findAttachedFilesByDocumentId(documentId)
                .stream()
                .map(p -> new DocumentFileAttachedListResDto(
                        p.getDocumentFileId(),
                        p.getFileId(),
                        p.getFileName(),
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
        // 문서 조회
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("문서를 찾을 수 없습니다."));

        // 문서 열람 권한 검증
        documentService.validateViewPermission(employeeId, document);

        // 문서-파일 관계 검증
        DocumentFile documentFile = documentFileRepository
                .findByDocument_IdAndFile_Id(documentId, fileId)
                .orElseThrow(() -> new ForbiddenException("문서에 포함되지 않은 파일입니다."));

        // 상태 검증
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

        // 이미지 스트리밍 반환
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

}
