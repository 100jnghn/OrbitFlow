package com.finalproj.orbitflow.chatbot.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatMessageDto
 * @since : 2026. 1. 6. 화요일
 */

@Getter
@Builder
public class ChatMessageDto {

    private Long messageId;
    private String role;
    private String content;
    private Object meta;
    private Instant createdAt;
}