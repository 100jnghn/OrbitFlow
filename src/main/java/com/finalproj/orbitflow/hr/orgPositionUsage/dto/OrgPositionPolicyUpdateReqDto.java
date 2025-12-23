package com.finalproj.orbitflow.hr.orgPositionUsage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgPositionPolicyUpdateReqDto
 * @since : 2025-12-23 화요일
 */
@Getter
public class OrgPositionPolicyUpdateReqDto {

    @NotNull
    private Long orgId;

    @NotEmpty
    private List<Long> positionCategoryIds;
}