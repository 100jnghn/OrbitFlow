package com.finalproj.orbitflow.approval.documentFile.dto;

import com.finalproj.orbitflow.approval.documentFile.enums.DocumentFileStatus;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFileAttachedListResDto
 * @since : 26. 1. 2. 금요일
 **/

public record DocumentFileAttachedListResDto(
        Long documentFileId,
        Long fileId,
        String fileName,
        Long fileSize,
        Long referenceTargetId,
        DocumentFileStatus status,
        String fieldId
) {
}