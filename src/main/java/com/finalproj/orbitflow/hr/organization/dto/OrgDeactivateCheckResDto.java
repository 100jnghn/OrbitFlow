package com.finalproj.orbitflow.hr.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgDeactivateCheckResDto
 * @since : 2025-12-26 금요일
 */
@Getter
@AllArgsConstructor
public class OrgDeactivateCheckResDto {
    private boolean canDeactivate;
    private String reason; // null이면 가능
}
