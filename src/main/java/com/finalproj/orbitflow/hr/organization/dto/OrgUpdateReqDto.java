package com.finalproj.orbitflow.hr.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgUpdateReqDto
 * @since : 2025-12-19 금요일
 */
@Getter
public class OrgUpdateReqDto {

    @NotNull
    private Long categoryId;

    private Long parentOrgId;

    @NotBlank
    private String name;

}
