package com.finalproj.orbitflow.hr.orgCategory.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgCategoryOrderUpdateReqDto
 * @since : 2025-12-19 금요일
 */
@Getter
public class OrgCategoryOrderUpdateReqDto {

    @NotEmpty
    private List<OrderItem> orders;

    @Getter
    public static class OrderItem {
        @NotNull
        private Long id;
    }
}
