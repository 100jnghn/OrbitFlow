package com.finalproj.orbitflow.approval.approvalLine.dto;

import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalRuleResDto
 * @since : 25. 12. 26. 금요일
 **/

@Data
@Builder
public class ApprovalRuleResDto {

    private Long approvalLineId;
    private Long documentId;

    private Long organizationId;
    private Long positionCategoryId;
    private Long approverId;

    private int orderNo;
    private ApprovalStatus approvalStatus;
    private String comment;
    private LocalDateTime decidedAt;

    public static ApprovalRuleResDto from(ApprovalLine approvalLine) {

        return ApprovalRuleResDto.builder()
                .approvalLineId(approvalLine.getId())
                .documentId(approvalLine.getDocument().getId())

                .organizationId(
                        approvalLine.getOrganization() != null
                                ? approvalLine.getOrganization().getId()
                                : null
                )
                .positionCategoryId(
                        approvalLine.getPositionCategory() != null
                                ? approvalLine.getPositionCategory().getId()
                                : null
                )
                .approverId(
                        approvalLine.getApprover() != null
                                ? approvalLine.getApprover().getId()
                                : null
                )

                .orderNo(approvalLine.getOrderNo())
                .approvalStatus(approvalLine.getStatus())
                .comment(approvalLine.getComment())
                .decidedAt(approvalLine.getDecidedAt())
                .build();
    }
}
