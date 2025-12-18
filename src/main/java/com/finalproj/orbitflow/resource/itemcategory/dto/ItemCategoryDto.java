package com.finalproj.orbitflow.resource.itemcategory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemCategoryDto
 * @since : 2025-12-17 오후 3:31 수요일
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCategoryDto {
    private Long id;
    private Long companyId;
    private String name;
}
