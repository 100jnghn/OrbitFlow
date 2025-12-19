package com.finalproj.orbitflow.hr.orgCategory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgCategoryCreateReqDto
 * @since : 2025-12-17 수요일
 */
@Getter
public class OrgCategoryCreateReqDto {

    @NotBlank
    private String name;

}
