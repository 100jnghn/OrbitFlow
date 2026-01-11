package com.finalproj.orbitflow.hr.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    private Long parentOrgId;

    @NotBlank
    @Size(max = 100)
    private String name;

    private Boolean isActive;
}
