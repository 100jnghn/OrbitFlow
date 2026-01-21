package com.finalproj.orbitflow.resource.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemResDto
 * @since : 2025-12-17 오후 5:14 수요일
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResDto {

    private Long itemId;
    private Long itemCategoryId;
    private String itemCategoryName;
    private String name;
    private String description;

    private Long statusId;
    private String statusCode;
    private String statusName;

    private Long fileId;

    private String uploaderName;
    private LocalDate createdAt;
}
