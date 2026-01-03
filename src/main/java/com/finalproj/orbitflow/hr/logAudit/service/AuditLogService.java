package com.finalproj.orbitflow.hr.logAudit.service;

import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.logAudit.entity.AuditLog;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEventType;
import com.finalproj.orbitflow.hr.logAudit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditLogService
 * @since : 2025-12-30 화요일
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(
            Company company,
            Employee actor,
            AuditEntityType entityType,
            Long entityId,
            AuditEventType eventType,
            Map<String, Object> beforeData,
            Map<String, Object> afterData
    ) {
        AuditLog log = AuditLog.create(
                company,
                actor,
                entityType,
                entityId,
                eventType,
                beforeData,
                afterData
        );

        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findEmployeeLogs(Long employeeId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                AuditEntityType.EMPLOYEE,
                employeeId
        );
    }

}
