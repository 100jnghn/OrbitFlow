package com.finalproj.orbitflow.chatbot.chatbot.repository;

import com.finalproj.orbitflow.chatbot.chatbot.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatMessageRepository
 * @since : 2026. 1. 6. 화요일
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage,String> {

    List<ChatMessage> findByConversationIdOrderByCreatedAtAscIdAsc(Long conversationId);
}
