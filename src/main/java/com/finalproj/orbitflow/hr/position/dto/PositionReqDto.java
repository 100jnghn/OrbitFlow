package com.finalproj.orbitflow.hr.position.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionReqDto
 * @since : 2025-12-22 월요일
 */
@Getter
@NoArgsConstructor
public class PositionReqDto {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String name;

    private Long parentPositionId;

    private Boolean isActive;
}
