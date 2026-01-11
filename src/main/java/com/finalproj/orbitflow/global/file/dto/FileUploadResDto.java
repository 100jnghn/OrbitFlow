package com.finalproj.orbitflow.global.file.dto;

import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FileUploadResDto
 * @since : 26. 1. 1. 목요일
 **/


@Getter
@Builder
public class FileUploadResDto {
    private Long id;
    private FileDomain domain;
    private String originFile;
    private String contentType;
    private Long fileSize;

    public static FileUploadResDto from(File file) {
        return FileUploadResDto.builder()
                .id(file.getId())
                .domain(file.getDomain())
                .originFile(file.getOriginFile())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .build();
    }
}