package com.finalproj.orbitflow.chatbot.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatConversationListDto
 * @since : 2026. 1. 6. 화요일
 */
@Getter
@Builder
public class ChatConversationListDto {

    private Long conversationId;
    private Long manualCategoryId;
    private String manualCategoryName;
    private String title;
    private String status;
    private Instant updatedAt;
}