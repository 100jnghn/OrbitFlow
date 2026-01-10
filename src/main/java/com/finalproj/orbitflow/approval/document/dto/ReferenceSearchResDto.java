package com.finalproj.orbitflow.approval.document.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ReferenceSearchResDto
 * @since : 26. 1. 10. 토요일
 **/


@Getter
@AllArgsConstructor
public class ReferenceSearchResDto {

    private Long documentId;
    private String title;
    private Instant approvedAt;
    private String writerName;
    private Long documentFileId;
}