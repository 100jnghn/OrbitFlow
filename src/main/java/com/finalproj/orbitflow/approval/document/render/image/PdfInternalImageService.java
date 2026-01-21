package com.finalproj.orbitflow.approval.document.render.image;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.document.file.service.DocumentFileService;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.document.signature.entity.DocumentSignature;
import com.finalproj.orbitflow.approval.document.signature.repository.DocumentSignatureRepository;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
import com.finalproj.orbitflow.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;


/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfInternalImageService
 * @since : 26. 1. 4. 일요일
 **/

@Service
@RequiredArgsConstructor
public class PdfInternalImageService {

    private final DocumentRepository documentRepository;
    private final DocumentFileService documentFileService;
    private final DocumentFileRepository documentFileRepository;
    private final DocumentSignatureRepository documentSignatureRepository;
    private final FileService fileService;

    public PdfImageResource loadApprovedDocumentComponentImage(
            Long documentId,
            Long documentFileId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new InvalidRequestException("Document not found"));

        if (document.getStatus() != DocumentStatus.APPROVED) {
            throw new InvalidRequestException("PDF 이미지 접근 불가");
        }


        DocumentFile documentFile = documentFileRepository.findById(documentFileId)
                        .orElseThrow(() -> new InvalidRequestException("Document file not found"));

        if (!documentFile.getDocument().getId().equals(documentId)) {
            throw new InvalidRequestException("문서-파일 불일치");
        }

        Resource resource =
                documentFileService.loadImage(documentFileId);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(documentFile.getFile().getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }


        return new PdfImageResource(resource, mediaType);
    }


    public PdfImageResource loadApprovedDocumentSignatureImage(
            Long documentId,
            Long approvalLineId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new InvalidRequestException("Document not found"));

        if (document.getStatus() != DocumentStatus.APPROVED) {
            throw new InvalidRequestException("서명 이미지 접근 불가");
        }

        DocumentSignature documentSignature = documentSignatureRepository.findByApprovalLine_Id(approvalLineId)
                .orElseThrow(() -> new InvalidRequestException("Signature not found"));


        if (!documentSignature.getDocument().getId().equals(documentId)) {
            throw new InvalidRequestException("문서-결재자 불일치");
        }

        Resource resource = fileService.loadAsResource(documentSignature.getSignatureFile());

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(documentSignature.getSignatureFile().getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }


        return new PdfImageResource(resource, mediaType);
    }
}
