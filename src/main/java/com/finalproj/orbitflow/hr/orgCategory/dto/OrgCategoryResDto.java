package com.finalproj.orbitflow.hr.orgCategory.dto;

import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgCategoryResDto
 * @since : 2025-12-17 수요일
 */

@Getter
@AllArgsConstructor
public class OrgCategoryResDto {
    private Long id;
    private String name;
    private Integer orderIndex;
    private Boolean isActive;

    public static OrgCategoryResDto from(OrgCategory entity) {
        return new OrgCategoryResDto(
                entity.getId(),
                entity.getName(),
                entity.getOrderIndex(),
                entity.getIsActive()
        );
    }
}
