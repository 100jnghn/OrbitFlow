package com.finalproj.orbitflow.approval.line.dto;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : RawApprovalRuleStepDto
 * @since : 25. 12. 25. 목요일
 **/



public record RawApprovalRuleStepDto(
        int step,
        Long organizationCategoryId,
        Long organizationId,
        Long positionCategoryId,
        Long employeeId
) {
}