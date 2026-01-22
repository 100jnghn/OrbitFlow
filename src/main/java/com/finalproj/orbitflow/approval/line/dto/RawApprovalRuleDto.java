package com.finalproj.orbitflow.approval.line.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : RawApprovalRuleDto
 * @since : 25. 12. 25. 목요일
 **/


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RawApprovalRuleDto {
    private List<RawApprovalRuleStepDto> approvalRule;
}