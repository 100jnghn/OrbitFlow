package com.finalproj.orbitflow.hr.logAudit.dto;

import com.finalproj.orbitflow.hr.logAudit.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditLogResDto
 * @since : 2025-12-30 화요일
 */
@Getter
@AllArgsConstructor
public class AuditLogResDto {
    private Long id;
    private String eventType;
    private String actorName;
    private String actorEmail;
    private String beforeData;
    private String afterData;
    private Instant createdAt;

    public static AuditLogResDto from(AuditLog log) {
        return new AuditLogResDto(
                log.getId(),
                log.getEventType().name(),
                log.getActor().getName(),
                log.getActor().getEmail(),
                log.getBeforeData(),
                log.getAfterData(),
                log.getCreatedAt()
        );
    }
}
