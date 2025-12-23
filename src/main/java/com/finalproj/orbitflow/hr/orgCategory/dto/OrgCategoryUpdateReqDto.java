package com.finalproj.orbitflow.hr.orgCategory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    // 이름 수정, 비활성화, 재활성화 가능

    @NotBlank
    @Size(max = 50)
    private String name;

    private Boolean isActive;
}
