package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentListResDto
 * @since : 25. 12. 22. 월요일
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentListResDto {
    private String title;
    private String templateGroupName;
    private int templateVersion;
    private Instant createdAt;
    private DocumentStatus status;
    private String approvalName;
    private Integer currentOrderNo;



}
