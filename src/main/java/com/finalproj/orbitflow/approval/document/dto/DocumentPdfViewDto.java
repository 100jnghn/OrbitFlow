package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentPdfViewDto
 * @since : 26. 1. 3. 토요일
 **/


@Getter
@Builder
public class DocumentPdfViewDto {

    private Long documentId;
    private String title;
    private DocumentStatus status;
    private LocalDateTime approvedAt;
    private LocalDateTime submittedAt;
    private String submittedBy;
    private String documentContentHtml;
}
