package com.finalproj.orbitflow.message.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.message.enums.MessageFolderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "message_recipient",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_message_recipient", columnNames = {"company_id", "message_id", "employee_id"})
        }
)
public class MessageRecipient extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    /** 수신자(또는 발신자 본인 SENT 보관용) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * INBOX / SENT만 존재
     * - 보관함 이동해도 folderType은 "원래 폴더"로 유지
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_folder_type", nullable = false, length = 50)
    private MessageFolderType messageFolderType;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "read_at")
    private Instant readAt;

    /** 보관함 여부 */
    @Column(name = "is_archived", nullable = false)
    private boolean isArchived;

    /** 소프트 삭제 */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ====================== 도메인 메서드 ======================

    public void markRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = Instant.now();
        }
    }

    public void archive() {
        this.isArchived = true;
    }

    public void unarchive() {
        this.isArchived = false;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
