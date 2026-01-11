package com.finalproj.orbitflow.hr.logAudit.dto;

import com.finalproj.orbitflow.hr.logAudit.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

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

    private String entityType;   // 대상
    private Long entityId;       // 대상 ID

    private String entityDisplay; // 화면 표시용


    private String eventType;
    private String actorName;
    private String actorEmail;

    private Map<String, Object> beforeData;
    private Map<String, Object> afterData;

    private Instant createdAt;

    public static AuditLogResDto from(AuditLog log) {
        return new AuditLogResDto(
                log.getId(),
                log.getEntityType().name(),
                log.getEntityId(),
                log.getEntityType().getDisplayName(),
                log.getEventType().name(),
                log.getActor().getName(),
                log.getActor().getEmail(),
                log.getBeforeData(),
                log.getAfterData(),
                log.getCreatedAt()
        );
    }
}
