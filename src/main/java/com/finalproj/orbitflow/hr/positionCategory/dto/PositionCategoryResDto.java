package com.finalproj.orbitflow.hr.positionCategory.dto;

import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionCategoryResDto
 * @since : 2025-12-22 월요일
 */

@Getter
@Builder
public class PositionCategoryResDto {
    private Long id;
    private String name;
    private Integer orderIndex;
    private Boolean isActive;


    public static PositionCategoryResDto from(PositionCategory entity) {
        return PositionCategoryResDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .orderIndex(entity.getOrderIndex())
                .isActive(entity.getIsActive())
                .build();
    }

}
