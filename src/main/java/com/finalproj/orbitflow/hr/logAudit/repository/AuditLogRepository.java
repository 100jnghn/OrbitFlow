package com.finalproj.orbitflow.hr.logAudit.repository;

import com.finalproj.orbitflow.hr.logAudit.entity.AuditLog;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
                SELECT a FROM AuditLog a
                JOIN a.actor act
                WHERE a.company.id = :companyId
                  AND (:entityType IS NULL OR a.entityType = :entityType)
                  AND (:eventType IS NULL OR a.eventType = :eventType)
                  AND (:actorName IS NULL OR act.name LIKE %:actorName%)
            """)
    Page<AuditLog> search(
            @Param("companyId") Long companyId,
            @Param("entityType") AuditEntityType entityType,
            @Param("eventType") AuditEventType eventType,
            @Param("actorName") String actorName,
            Pageable pageable
    );

}