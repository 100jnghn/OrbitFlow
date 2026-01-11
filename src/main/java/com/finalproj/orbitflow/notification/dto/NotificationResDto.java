package com.finalproj.orbitflow.notification.dto;

import com.finalproj.orbitflow.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : NotificationResDto
 * @since : 2026-01-02 오후 7:08 금요일
 */
@Data
@AllArgsConstructor
public class NotificationResDto {

    private Long notificationId;
    private String type;
    private String content;
    private Boolean isRead;
    private String url;
    private Instant createdAt;

    // entity -> dto
    public static NotificationResDto fromEntity(Notification notification) {
        return new NotificationResDto(
                notification.getId(),
                notification.getType().getDescription(),
                notification.getContent(),
                notification.getIsRead(),
                notification.getUrl(),
                notification.getCreatedAt()
        );
    }
}
