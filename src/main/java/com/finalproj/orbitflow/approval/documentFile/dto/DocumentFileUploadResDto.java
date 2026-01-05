package com.finalproj.orbitflow.approval.documentFile.dto;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFileUploadResDto
 * @since : 26. 1. 2. 금요일
 **/


public record DocumentFileUploadResDto (
        Long documentFileId,
        Long fileId,
        String fileName,
        Long fileSize
){}