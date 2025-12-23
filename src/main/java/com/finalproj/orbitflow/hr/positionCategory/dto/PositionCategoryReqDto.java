package com.finalproj.orbitflow.hr.positionCategory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionCategoryReqDto
 * @since : 2025-12-22 월요일
 */
@Getter
@NoArgsConstructor
public class PositionCategoryReqDto {
    private String name;
    private Long orgCategoryId;
    private Boolean isActive;
}
