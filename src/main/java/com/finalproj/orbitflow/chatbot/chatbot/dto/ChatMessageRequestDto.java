package com.finalproj.orbitflow.chatbot.chatbot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatMessageRequestDto
 * @since : 2026. 1. 6. 화요일
 */

@Getter
@NoArgsConstructor
public class ChatMessageRequestDto {

    /**
     * 사용자가 입력한 질문 텍스트
     */
    private String content;
}
