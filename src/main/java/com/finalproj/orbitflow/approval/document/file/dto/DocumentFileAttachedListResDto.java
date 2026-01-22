package com.finalproj.orbitflow.approval.document.file.dto;

import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;

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
        String displayName,
        String writerName,
        Long fileSize,
        Long referenceTargetId,
        DocumentFileStatus status,
        String fieldId
) {
}
