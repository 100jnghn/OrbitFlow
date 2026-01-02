package com.finalproj.orbitflow.approval.documentFile.service;

import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentFile.dto.DocumentFileAttachedListResDto;
import com.finalproj.orbitflow.approval.documentFile.dto.DocumentFileUploadResDto;
import com.finalproj.orbitflow.approval.documentFile.entity.DocumentFile;
import com.finalproj.orbitflow.approval.documentFile.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.documentFile.enums.ReferenceType;
import com.finalproj.orbitflow.approval.documentFile.repository.DocumentFileRepository;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.global.file.service.FileService;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    private final EmployeeRepository employeeRepository;
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
                uploaderId,
                FileDomain.DOCUMENT,
                multipartFile
        );

        DocumentFile documentFile = DocumentFile.builder()
                .document(document)
                .file(file)
                .referenceType(ReferenceType.ATTACHMENT)
                .status(DocumentFileStatus.TEMP)
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
        if (document.getStatus() == DocumentStatus.DRAFT) {
            if (!document.getWriter().getId().equals(employeeId)) {
                throw new InvalidRequestException("첨부 파일 업로드 권한 없음");
            }
        } else {
            boolean isApprover = approvalLineRepository
                    .findByDocument_IdOrderByOrderNoAsc(documentId)
                    .stream()
                    .anyMatch(line -> line.getApprover().getId().equals(employeeId));

            if (!isApprover) {
                throw new InvalidRequestException("첨부 파일 업로드 권한 없음");
            }
        }
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
                        p.getStatus()
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
}
