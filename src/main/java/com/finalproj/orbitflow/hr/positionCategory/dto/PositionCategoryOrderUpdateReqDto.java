package com.finalproj.orbitflow.hr.positionCategory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionCategoryOrderUpdateReqDto
 * @since : 2025-12-22 월요일
 */

@Getter
public class PositionCategoryOrderUpdateReqDto {

    private List<OrderItem> orders;

    @Getter
    @NoArgsConstructor
    public static class OrderItem {
        private Long id;
    }
}
