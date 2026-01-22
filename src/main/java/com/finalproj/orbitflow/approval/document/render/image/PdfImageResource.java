package com.finalproj.orbitflow.approval.document.render.image;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfImageResponse
 * @since : 26. 1. 4. 일요일
 **/


public record PdfImageResource(
        Resource resource,
        MediaType mediaType
) {
}