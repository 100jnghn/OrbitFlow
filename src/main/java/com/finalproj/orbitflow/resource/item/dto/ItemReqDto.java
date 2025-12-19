package com.finalproj.orbitflow.resource.item.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemReqDto
 * @since : 2025-12-17 오후 5:13 수요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemReqDto {

    private Long itemCategoryId;
    private String name;
    private String description;
    private Long statusId;

    private MultipartFile imgFile;
}