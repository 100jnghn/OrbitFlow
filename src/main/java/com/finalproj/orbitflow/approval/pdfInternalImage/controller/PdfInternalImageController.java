package com.finalproj.orbitflow.approval.pdfInternalImage.controller;

import com.finalproj.orbitflow.approval.pdfInternalImage.dto.PdfImageResponse;
import com.finalproj.orbitflow.approval.pdfInternalImage.service.PdfInternalImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfInternalImageController
 * @since : 26. 1. 3. 토요일
 **/


@RestController
@RequestMapping("/internal/pdf")
@RequiredArgsConstructor
public class PdfInternalImageController {

    private final PdfInternalImageService pdfInternalImageService;

    @GetMapping("/documents/{documentId}/images/{documentFileId}")
    public ResponseEntity<Resource> servePdfImage(
            @PathVariable Long documentId,
            @PathVariable Long documentFileId
    ) {
        PdfImageResponse res =
                pdfInternalImageService.loadApprovedDocumentImage(
                        documentId,
                        documentFileId
                );

        return ResponseEntity.ok()
                .contentType(res.mediaType())
                .body(res.resource());
    }
}
