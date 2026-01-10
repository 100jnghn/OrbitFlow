package com.finalproj.orbitflow.hr.logAudit.service;

import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.logAudit.dto.AuditLogResDto;
import com.finalproj.orbitflow.hr.logAudit.entity.AuditLog;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEventType;
import com.finalproj.orbitflow.hr.logAudit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final AuditEntityNameResolver entityNameResolver;

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
                AuditValueNormalizer.normalizeMap(beforeData),
                AuditValueNormalizer.normalizeMap(afterData)
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

    @Transactional(readOnly = true)
    public Page<AuditLogResDto> search(
            Long companyId,
            AuditEntityType entityType,
            AuditEventType eventType,
            String actorName,
            Pageable pageable
    ) {
        return auditLogRepository.search(companyId, entityType, eventType, actorName, pageable)
                .map(log -> {

                    String entityName =
                            entityNameResolver.resolve(log.getEntityType(), log.getEntityId());

                    String display =
                            log.getEntityType().getDisplayName() + " · " + entityName;

                    return new AuditLogResDto(
                            log.getId(),
                            log.getEntityType().name(),
                            log.getEntityId(),
                            display,
                            log.getEventType().name(),
                            log.getActor().getName(),
                            log.getActor().getEmail(),
                            log.getBeforeData(),
                            log.getAfterData(),
                            log.getCreatedAt()
                    );
                });
    }


    @Transactional(readOnly = true)
    public AuditLog findById(Long id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AuditLog not found"));
    }

}
