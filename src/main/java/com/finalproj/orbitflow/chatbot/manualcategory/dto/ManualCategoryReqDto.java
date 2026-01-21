package com.finalproj.orbitflow.chatbot.manualcategory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매뉴얼 카테고리 응답 DTO
 *
 * @author : rlagkdus
 * @filename : ManualCategoryReqDto
 * @since : 2025. 12. 30. 화요일
 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualCategoryReqDto {

    private String categoryName;
    private String description;
    private Integer sortOrder;
}
