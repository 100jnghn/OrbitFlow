package com.finalproj.orbitflow.hr.orgPositionUsage.dto;

import com.finalproj.orbitflow.hr.orgPositionUsage.entity.OrgPositionUsage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgPositionUsageResDto
 * @since : 2025-12-23 화요일
 */
@Getter
@AllArgsConstructor
public class OrgPositionUsageResDto {

    private Long positionCategoryId;
    private String positionCategoryName;
    private boolean isHead;

    public static OrgPositionUsageResDto from(OrgPositionUsage entity) {
        return new OrgPositionUsageResDto(
                entity.getPositionCategory().getId(),
                entity.getPositionCategory().getName(),
                entity.getPositionCategory().getIsHead()
        );
    }
}