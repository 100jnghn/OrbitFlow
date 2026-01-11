package com.finalproj.orbitflow.chatbot.manualCategory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualCategoryReqDto {

    private String categoryName; // 카테고리 이름
    private String description;  // 카테고리 설명 (선택 사항)
    private Integer sortOrder;   // 출력 순서
}
