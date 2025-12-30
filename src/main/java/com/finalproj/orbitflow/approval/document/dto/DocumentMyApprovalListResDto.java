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

    /* ===== 문서 기본 정보 ===== */
    private Long documentId;
    private String documentTitle;
    private String templateName;
    private String writerName;
    private Instant createdAt;

    /* ===== 내 결재 정보 ===== */
    private ApprovalStatus myApprovalStatus;
    private Instant processedAt; // 내가 승인/반려한 시각 (없으면 null)

    /* ===== 목록 표시용 담당자 ===== */
    private String displayApproverName;
    private String displayApproverOrgName;
    private String displayApproverPositionName;

    /* ===== 진행 상태 보조 정보 ===== */
    private Integer remainingBeforeMyTurn; // 0이면 내 차례, 완료 문서는 null
}