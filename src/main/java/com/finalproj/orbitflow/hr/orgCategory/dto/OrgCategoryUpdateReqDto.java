package com.finalproj.orbitflow.hr.orgCategory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgCategoryUpdateReqDto
 * @since : 2025-12-17 수요일
 */
@Getter
public class OrgCategoryUpdateReqDto {

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer orderIndex;
}
