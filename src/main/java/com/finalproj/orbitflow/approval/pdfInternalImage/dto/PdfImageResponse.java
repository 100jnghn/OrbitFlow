package com.finalproj.orbitflow.approval.pdfInternalImage.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfImageResponse
 * @since : 26. 1. 4. 일요일
 **/


public record PdfImageResponse(
        Resource resource,
        MediaType mediaType
) {
}