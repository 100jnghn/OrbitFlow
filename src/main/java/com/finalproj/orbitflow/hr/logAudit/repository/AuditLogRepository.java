package com.finalproj.orbitflow.hr.logAudit.repository;

import com.finalproj.orbitflow.hr.logAudit.entity.AuditLog;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditLogRepository
 * @since : 2025-12-16 화요일
 */

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            AuditEntityType entityType,
            Long entityId
    );

}