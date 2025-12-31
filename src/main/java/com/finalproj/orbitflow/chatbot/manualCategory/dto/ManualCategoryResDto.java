package com.finalproj.orbitflow.chatbot.manualCategory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매뉴얼 카테고리 응답 DTO
 *
 * @author : rlagkdus
 * @filename : ManualCategoryResDto
 * @since : 2025. 12. 30. 화요일
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualCategoryResDto {
    private Long id;
    private String categoryName;
    private String description;
    private Integer sortOrder;
}
