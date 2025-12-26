package com.finalproj.orbitflow.message.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "message_recipient",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "message_id", "employee_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageRecipient extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_folder_type", nullable = false)
    private MessageFolderType folderType;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "read_date")
    private Instant readDate;

    @Builder
    public MessageRecipient(Long companyId, Message message, Employee recipient) {
        this.companyId = companyId;
        this.message = message;
        this.recipient = recipient;
        this.folderType = MessageFolderType.INBOX;
        this.isRead = false;
        this.isArchived = false;
    }

    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readDate = Instant.now();
        }
    }

    public void archive() {
        this.isArchived = true;
        this.folderType = MessageFolderType.ARCHIVE;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }
}
