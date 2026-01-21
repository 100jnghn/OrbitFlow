package com.finalproj.orbitflow.chatbot.chatbot.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatConversation
 * @since : 2026. 1. 6. 화요일
 */


@Getter
@Entity
@Table(
        name = "chat_conversation",
        indexes = {
                @Index(name = "idx_chat_conv_employee_updated", columnList = "company_id, employee_id, updated_at"),
                @Index(name = "idx_chat_conv_category", columnList = "company_id, manual_category_id, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatConversation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "manual_category_id")
    private Long manualCategoryId;

    @Column(name = "manual_category_name", length = 255)
    private String manualCategoryName;

    @Column(name = "title", length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted;

    public enum Status { ACTIVE, CLOSED }

    public void close() {
        this.status = Status.CLOSED;
    }

    public void setTitleIfEmpty(String newTitle) {
        if (this.title == null || this.title.isBlank()) {
            this.title = newTitle;
        }
    }
}