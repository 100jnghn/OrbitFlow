package com.finalproj.orbitflow.hr.organization.dto;

import com.finalproj.orbitflow.hr.organization.entity.Organization;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgResDto
 * @since : 2025-12-19 금요일
 */
@Getter
@AllArgsConstructor
public class OrgResDto {
    private Long id;
    private Long categoryId;
    private Long parentOrgId;
    private String name;
    private Integer orderIndex;
    private Boolean isActive;

    public static OrgResDto from(Organization entity) {
        return new OrgResDto(
                entity.getId(),
                entity.getCategoryId(),
                entity.getParentOrgId(),
                entity.getName(),
                entity.getOrderIndex(),
                entity.getIsActive()
        );
    }

}
