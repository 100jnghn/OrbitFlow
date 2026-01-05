package com.finalproj.orbitflow.approval.documentFile.repository;

import com.finalproj.orbitflow.approval.documentFile.enums.DocumentFileStatus;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFileAttachedProjection
 * @since : 26. 1. 2. 금요일
 **/


public interface DocumentFileAttachedProjection {
    Long getDocumentFileId();
    Long getFileId();
    String getFileName();
    Long getFileSize();
    Long getReferenceTargetId();
    DocumentFileStatus getStatus();
    String getFieldId();
}