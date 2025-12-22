package com.finalproj.orbitflow.hr.position.dto;

import com.finalproj.orbitflow.hr.position.entity.Position;
import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionResDto
 * @since : 2025-12-22 월요일
 */
@Getter
@Builder
public class PositionResDto {

    private Long id;
    private String name;
    private Integer orderIndex;
    private Boolean isActive;

    private Long categoryId;
    private String categoryName;

    private Long parentPositionId;
    private String parentPositionName;

    public static PositionResDto from(Position p) {
        return PositionResDto.builder()
                .id(p.getId())
                .name(p.getName())
                .orderIndex(p.getOrderIndex())
                .isActive(p.getIsActive())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .parentPositionId(p.getParentPosition() != null ? p.getParentPosition().getId() : null)
                .parentPositionName(p.getParentPosition() != null ? p.getParentPosition().getName() : null)
                .build();
    }
}
