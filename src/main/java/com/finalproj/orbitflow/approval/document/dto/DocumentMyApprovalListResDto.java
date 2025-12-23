package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentMyApprovalListResDto
 * @since : 25. 12. 23. 화요일
 **/

@Getter
@AllArgsConstructor
@Builder
public class DocumentMyApprovalListResDto {
    private Long documentId;
    private String documentTitle;
    private String templateName;
    private String writerName;
    private Instant createdAt;

    private Instant processedAt;          // 내가 결재한 날짜 (없으면 null)
    private String currentApproverName;   // 현재 결재자
    private ApprovalStatus myApprovalStatus;
}
