package com.finalproj.orbitflow.hr.logAudit.repository;

import com.finalproj.orbitflow.hr.logAudit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditLogRepository
 * @since : 2025-12-16 화요일
 */

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}