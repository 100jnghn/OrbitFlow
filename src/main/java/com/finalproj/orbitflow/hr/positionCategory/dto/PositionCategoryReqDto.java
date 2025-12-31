package com.finalproj.orbitflow.hr.positionCategory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotBlank
    @Size(max = 50)
    private String name;

    @NotNull
    private Long orgCategoryId;

    private Long parentPositionId;

    @NotNull
    private Boolean isHead;

    // 수정 시만 사용 (create 시 null 허용)
    private Boolean isActive;
}
