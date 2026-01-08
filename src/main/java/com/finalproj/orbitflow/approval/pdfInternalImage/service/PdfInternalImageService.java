package com.finalproj.orbitflow.approval.pdfInternalImage.service;

import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.documentFile.entity.DocumentFile;
import com.finalproj.orbitflow.approval.documentFile.repository.DocumentFileRepository;
import com.finalproj.orbitflow.approval.documentFile.service.DocumentFileService;
import com.finalproj.orbitflow.approval.pdfInternalImage.dto.PdfImageResponse;
import com.finalproj.orbitflow.global.exception.InvalidRequestException;
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

    public PdfImageResponse loadApprovedDocumentComponentImage(
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


        return new PdfImageResponse(resource, mediaType);
    }


    public PdfImageResponse loadApprovedDocumentSignatureImage(
            Long documentId,
            Long approvalLineId
    ) {
        //TODO
        //Document document = documentRepository.findById(documentId)
        //        .orElseThrow(() -> new InvalidRequestException("Document not found"));
        //
        //if (document.getStatus() != DocumentStatus.APPROVED) {
        //    throw new InvalidRequestException("PDF 이미지 접근 불가");
        //}
        //
        //
        //DocumentFile documentFile = documentFileRepository.findById(approvalLineId)
        //        .orElseThrow(() -> new InvalidRequestException("Document file not found"));
        //
        //
        //
        //if (!documentFile.getDocument().getId().equals(documentId)) {
        //    throw new InvalidRequestException("문서-파일 불일치");
        //}
        //
        //Resource resource =
        //        documentFileService.loadImage(approvalLineId);
        //
        //MediaType mediaType;
        //try {
        //    mediaType = MediaType.parseMediaType(documentFile.getFile().getContentType());
        //} catch (Exception e) {
        //    mediaType = MediaType.APPLICATION_OCTET_STREAM;
        //}
        //
        //
        //return new PdfImageResponse(resource, mediaType);
        return null;
    }
}
