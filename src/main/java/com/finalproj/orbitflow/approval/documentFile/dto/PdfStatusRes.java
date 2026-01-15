package com.finalproj.orbitflow.approval.documentFile.dto;

import com.finalproj.orbitflow.approval.documentFile.enums.PdfStatus;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfStatusRes
 * @since : 26. 1. 15. 목요일
 **/


public record PdfStatusRes(
        PdfStatus status,
        Long pdfFileId
) {
}
