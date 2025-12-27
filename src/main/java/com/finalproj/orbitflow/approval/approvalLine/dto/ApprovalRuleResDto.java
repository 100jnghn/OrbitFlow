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

    // === 식별자 ===
    private Long organizationId;
    private Long positionCategoryId;
    private Long approverId;

    // === 표시용 정보 ===
    private String organizationName;
    private String positionName;

    private String approverName;
    private String approverEmployeeNo;

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
                .organizationName(
                        approvalLine.getOrganization() != null
                                ? approvalLine.getOrganization().getName()
                                : null
                )

                .positionCategoryId(
                        approvalLine.getPositionCategory() != null
                                ? approvalLine.getPositionCategory().getId()
                                : null
                )
                .positionName(
                        approvalLine.getPositionCategory() != null
                                ? approvalLine.getPositionCategory().getName()
                                : null
                )

                .approverId(
                        approvalLine.getApprover() != null
                                ? approvalLine.getApprover().getId()
                                : null
                )
                .approverName(
                        approvalLine.getApprover() != null
                                ? approvalLine.getApprover().getName()
                                : null
                )
                .approverEmployeeNo(
                        approvalLine.getApprover() != null
                                ? approvalLine.getApprover().getEmployeeNo()
                                : null
                )

                .orderNo(approvalLine.getOrderNo())
                .approvalStatus(approvalLine.getStatus())
                .comment(approvalLine.getComment())
                .decidedAt(approvalLine.getDecidedAt())
                .build();
    }

}
