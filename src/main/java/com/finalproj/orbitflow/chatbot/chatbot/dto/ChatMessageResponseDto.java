package com.finalproj.orbitflow.chatbot.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatMessageResponseDto
 * @since : 2026. 1. 6. 화요일
 */

@Getter
@Builder
public class ChatMessageResponseDto {
    private ChatMessageDto assistant;
}