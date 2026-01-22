package com.finalproj.orbitflow.approval.line.dto;

import com.finalproj.orbitflow.approval.line.enums.RuleTargetType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : RuleTarget
 * @since : 25. 12. 25. 목요일
 **/


@Data
@AllArgsConstructor
public class RuleTarget {

    private RuleTargetType type;

    private Long organizationCategoryId;
    private Long organizationId;
    private Long positionId;
    private Long employeeId;

    public static RuleTarget orgCategoryChain(Long organizationCategoryId) {
        return new RuleTarget(
                RuleTargetType.ORG_CATEGORY_CHAIN,
                organizationCategoryId,
                null,
                null,
                null
        );
    }

    public static RuleTarget orgAndPosition(Long organizationId, Long positionId) {
        return new RuleTarget(
                RuleTargetType.ORG_AND_POSITION,
                null,
                organizationId,
                positionId,
                null
        );
    }

    public static RuleTarget fixedEmployee(
            Long employeeId,
            Long organizationId,
            Long positionCategoryId
    ) {
        return new RuleTarget(
                RuleTargetType.FIXED_EMPLOYEE,
                null,
                organizationId,
                positionCategoryId,
                employeeId
        );
    }

}
