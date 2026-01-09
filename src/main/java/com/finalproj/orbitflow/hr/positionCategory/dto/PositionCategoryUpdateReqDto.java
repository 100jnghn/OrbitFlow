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
 * @filename : PositionCategoryUpdateReqDto
 * @since : 2026-01-09 금요일
 */
@Getter
@NoArgsConstructor
public class PositionCategoryUpdateReqDto {

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotNull
    private Boolean isActive;
}
