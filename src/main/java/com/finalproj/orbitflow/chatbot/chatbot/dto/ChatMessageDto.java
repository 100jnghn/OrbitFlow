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

    private String role;   // USER / ASSISTANT / SYSTEM
    private String content;

    /**
     * meta_json 그대로 내려줌
     * - model
     * - token_usage
     * - source_manual_ids
     * 등 확장용
     */
    private Object meta;

    private Instant createdAt;
}