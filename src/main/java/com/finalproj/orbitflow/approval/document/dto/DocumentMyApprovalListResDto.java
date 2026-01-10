package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentMyApprovalListResDto
 * @since : 25. 12. 23. 화요일
 **/

@Data
@Builder
@AllArgsConstructor
public class DocumentMyApprovalListResDto {

    /* ===== 문서 기본 정보 ===== */
    private Long documentId;
    private String documentTitle;
    private String templateName;
    private String writerName;
    private Instant createdAt;

    /* ===== 문서 상태 (화면 표시용) ===== */
    private String documentDisplayStatus;

    /* ===== 내 결재 정보 ===== */
    private ApprovalStatus myApprovalStatus;
    private Instant processedAt;

    /* ===== 목록 표시용 담당자 ===== */
    private String displayApproverName;
    private String displayApproverOrgName;
    private String displayApproverPositionName;

    /* ===== 진행 상태 보조 정보 ===== */
    private Integer remainingBeforeMyTurn;

    /**
     * ✅ QueryDSL 전용 생성자
     * ⚠ SELECT 절 순서와 1:1로 맞아야 함
     */
    public DocumentMyApprovalListResDto(
            Long documentId,
            String documentTitle,
            String templateName,
            String writerName,
            Instant createdAt,

            LocalDateTime decidedAt,
            String documentDisplayStatus,

            String displayApproverOrgName,
            String displayApproverPositionName,
            String displayApproverName,

            ApprovalStatus myApprovalStatus,
            Integer remainingBeforeMyTurn
    ) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.templateName = templateName;
        this.writerName = writerName;
        this.createdAt = createdAt;
        this.documentDisplayStatus = documentDisplayStatus;

        this.processedAt = decidedAt != null
                ? decidedAt.atZone(ZoneId.systemDefault()).toInstant()
                : null;

        this.displayApproverOrgName = displayApproverOrgName;
        this.displayApproverPositionName = displayApproverPositionName;
        this.displayApproverName = displayApproverName;

        this.myApprovalStatus = myApprovalStatus;
        this.remainingBeforeMyTurn = remainingBeforeMyTurn;
    }
}
