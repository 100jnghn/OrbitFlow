package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;

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
public class DocumentListResDto {
    private String title;
    private String templateGroupName;
    private int templateVersion;
    private Instant createdAt;
    private DocumentStatus status;
    private String approvalName;
    private Integer currentOrderNo;


    public DocumentListResDto(
            String title,
            String templateGroupName,
            int templateVersion,
            Instant createdAt,
            DocumentStatus status,
            String approvalName,
            Integer currentOrderNo
    ) {
        this.title = title;
        this.templateGroupName = templateGroupName;
        this.templateVersion = templateVersion;
        this.createdAt = createdAt;
        this.status = status;
        this.approvalName = approvalName;
        this.currentOrderNo = currentOrderNo;
    }
}
