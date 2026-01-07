package com.finalproj.orbitflow.chatbot.chatbot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatConversationRequestDto
 * @since : 2026. 1. 6. 화요일
 */

@Getter
@NoArgsConstructor
public class ChatConversationRequestDto {

    /**
     * 선택한 매뉴얼 카테고리 ID
     * - null 허용 (카테고리 없이 시작하는 경우)
     */
    private Long manualCategoryId;
}
