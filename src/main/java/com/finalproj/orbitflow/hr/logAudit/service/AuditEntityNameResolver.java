package com.finalproj.orbitflow.hr.logAudit.service;

import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.positionCategory.repository.PositionCategoryRepository;
import com.finalproj.orbitflow.hr.rank.repository.RankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditEntityNameResolver
 * @since : 2026-01-06 화요일
 */
@Component
@RequiredArgsConstructor
public class AuditEntityNameResolver {

    private final EmployeeRepository employeeRepository;
    private final OrgRepository organizationRepository;
    private final RankRepository rankRepository;
    private final PositionCategoryRepository positionRepository;
    private final CompanyRepository companyRepository;

    public String resolve(AuditEntityType type, Long entityId) {
        return switch (type) {
            case EMPLOYEE ->
                    employeeRepository.findById(entityId)
                            .map(e -> e.getName())
                            .orElse("알 수 없음");
            case ORGANIZATION ->
                    organizationRepository.findById(entityId)
                            .map(o -> o.getName())
                            .orElse("알 수 없음");
            case HR_RANK ->
                    rankRepository.findById(entityId)
                            .map(r -> r.getName())
                            .orElse("알 수 없음");
            case POSITION ->
                    positionRepository.findById(entityId)
                            .map(p -> p.getName())
                            .orElse("알 수 없음");
            case COMPANY ->
                    companyRepository.findById(entityId)
                            .map(c -> c.getName())
                            .orElse("알 수 없음");
            default -> "알 수 없음";
        };
    }
}
