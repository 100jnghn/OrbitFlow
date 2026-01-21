package com.finalproj.orbitflow.approval.line.dto;

import com.finalproj.orbitflow.approval.line.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.line.enums.ApprovalStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalLineViewResDto
 * @since : 25. 12. 28. 일요일
 **/


@Getter
@Builder
public class ApprovalLineViewResDto {

    private int orderNo;
    private String approverDisplay;
    private ApprovalStatus status;

    private String comment;

    private LocalDateTime decidedAt;

    public static ApprovalLineViewResDto from(ApprovalLine line) {

        String organizationName =
                line.getOrganization() != null
                        ? line.getOrganization().getName()
                        : "";

        String positionName =
                line.getPositionCategory() != null
                        ? line.getPositionCategory().getName()
                        : "";

        String approverName =
                line.getApprover() != null
                        ? line.getApprover().getName()
                        : "미지정";

        String display =
                organizationName + " / " +
                        positionName + " / " +
                        approverName;

        return ApprovalLineViewResDto.builder()
                .orderNo(line.getOrderNo())
                .approverDisplay(display)
                .status(line.getStatus())
                .comment(line.getComment())
                .decidedAt(line.getDecidedAt())
                .build();
    }
}