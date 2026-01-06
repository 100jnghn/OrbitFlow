package com.finalproj.orbitflow.notification.dto;

import com.finalproj.orbitflow.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * NotificationMessageDto는 HTTP가 아닌 메시지(Event) DTO
 *
 * @author : 종훈
 * @filename : NotificationMessageDto
 * @since : 2026-01-02 오후 5:36 금요일
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessageDto {

    private Long notificationId;
    private Long companyId;
    private Long employeeId;
    private String type;
    private String content;
    private Boolean isRead;
    private String url;
    private Instant createdAt;

    /**
     * entity to dto
     */
    public static NotificationMessageDto fromEntity(Notification notification) {
        return NotificationMessageDto.builder()
                .notificationId(notification.getId())
                .companyId(notification.getCompany().getId())
                .employeeId(notification.getReceiver().getId())
                .type(notification.getType().getDescription())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .url(notification.getUrl())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
