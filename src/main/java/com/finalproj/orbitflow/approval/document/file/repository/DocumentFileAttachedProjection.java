package com.finalproj.orbitflow.approval.document.file.repository;

import com.finalproj.orbitflow.approval.document.file.enums.DocumentFileStatus;
import com.finalproj.orbitflow.approval.document.file.enums.ReferenceType;

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

    String getDisplayName();

    String getWriterName();

    Long getFileSize();

    Long getReferenceTargetId();

    ReferenceType getReferenceType();

    DocumentFileStatus getStatus();

    String getFieldId();
}
