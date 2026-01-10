package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import lombok.AllArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
public class DocumentListResDto {
    private Long documentId;
    private String title;
    private String templateGroupName;
    private Integer templateVersion;
    private Instant createdAt;
    private DocumentStatus status;
    private Integer currentApprovalOrder;
    private Long totalApprovalCount;
    private String currentApproverOrgName;
    private String currentApproverPositionName;
    private String currentApproverName;
    private Boolean hasRevision;
}
