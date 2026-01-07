package com.finalproj.orbitflow.chatbot.chatbot.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatMessage
 * @since : 2026. 1. 6. 화요일
 */
@Getter
@Entity
@Table(
        name = "chat_message",
        indexes = {
                @Index(name = "idx_chat_msg_conv_created", columnList = "conversation_id, created_at"),
                @Index(name = "idx_chat_msg_company_created", columnList = "company_id, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_json", columnDefinition = "json")
    private JsonNode metaJson;

    public enum Role { USER, ASSISTANT, SYSTEM }
}