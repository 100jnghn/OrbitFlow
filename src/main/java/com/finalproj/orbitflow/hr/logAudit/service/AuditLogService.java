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

import java.util.LinkedHashMap;
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
    public AuditLogResDto findAdminAuditLog(Long id, Long companyId) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AuditLog not found"));

        if (!log.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("다른 회사의 감사 로그입니다.");
        }

        return toAdminDto(log);
    }


    @Transactional(readOnly = true)
    public Page<AuditLogResDto> searchAdminAuditLogs(
            Long companyId,
            String actorName,
            Pageable pageable
    ) {

        List<AuditEntityType> allowedEntities = List.of(
                AuditEntityType.ORGANIZATION,
                AuditEntityType.EMPLOYEE,
                AuditEntityType.HR_RANK,
                AuditEntityType.POSITION,
                AuditEntityType.ORG_POSITION_USAGE
        );

        List<AuditEventType> allowedEvents = List.of(
                AuditEventType.MOVE,
                AuditEventType.ASSIGN,
                AuditEventType.UNASSIGN,
                AuditEventType.STATUS_CHANGE,
                AuditEventType.ACTIVATE,
                AuditEventType.DEACTIVATE,
                AuditEventType.CREATE,
                AuditEventType.UPDATE
        );

        return auditLogRepository
                .searchAdminAuditLogs(
                        companyId,
                        allowedEntities,
                        allowedEvents,
                        actorName,
                        pageable
                )
                .map(this::toAdminDto);
    }


    @Transactional(readOnly = true)
    public AuditLog findById(Long id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AuditLog not found"));
    }

    private AuditLogResDto toAdminDto(AuditLog log) {

        String entityName =
                entityNameResolver.resolve(log.getEntityType(), log.getEntityId());

        String entityDisplay =
                log.getEntityType().getDisplayName() + " · " + entityName;

        Map<String, Object> before = convertReadable(log.getBeforeData());
        Map<String, Object> after  = convertReadable(log.getAfterData());

        return new AuditLogResDto(
                log.getId(),
                log.getEntityType().name(),
                log.getEntityId(),
                entityDisplay,
                log.getEventType().getDisplayName(),
                log.getActor().getName(),
                log.getActor().getEmail(),
                before,
                after,
                log.getCreatedAt()
        );
    }

    private Map<String, Object> convertReadable(Map<String, Object> data) {
        if (data == null) return null;

        Map<String, Object> result = new LinkedHashMap<>();

        for (var entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            Object readableValue = switch (key) {

                case "isHead" ->
                        entityNameResolver.booleanDisplay(key, value);

                case "orgId" ->
                        entityNameResolver.organizationName(toLong(value));

                case "orgCategoryId" ->
                        entityNameResolver.orgCategoryName(toLong(value));

                case "parentPositionId", "positionCategoryId" ->
                        entityNameResolver.positionName(toLong(value));

                case "parentRankId","rankId" ->
                        entityNameResolver.rankName(toLong(value));

                // enum 한글화
                case "status", "employmentType", "gender", "role" ->
                        entityNameResolver.enumDisplay(key, value);

                // 일반 필드
                default -> value;
            };

            result.put(key, readableValue);
        }

        return result;
    }


    private Long toLong(Object value) {
        if (value == null) return null;

        if (value instanceof Number n) {
            return n.longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
