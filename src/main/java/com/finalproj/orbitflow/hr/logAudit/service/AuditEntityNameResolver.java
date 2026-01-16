package com.finalproj.orbitflow.hr.logAudit.service;

import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType;
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
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
    private final OrgCategoryRepository orgCategoryRepository;

    /* 엔티티 이름 (헤더용) */
    public String resolve(AuditEntityType type, Long entityId) {
        return switch (type) {
            case EMPLOYEE -> employeeRepository.findById(entityId)
                    .map(e -> e.getName()).orElse("알 수 없음");
            case ORGANIZATION -> organizationRepository.findById(entityId)
                    .map(o -> o.getName()).orElse("알 수 없음");
            case HR_RANK -> rankRepository.findById(entityId)
                    .map(r -> r.getName()).orElse("알 수 없음");
            case POSITION -> positionRepository.findById(entityId)
                    .map(p -> p.getName()).orElse("알 수 없음");
            case COMPANY -> companyRepository.findById(entityId)
                    .map(c -> c.getName()).orElse("알 수 없음");
            case SIGNATURE -> "전자서명";

            default -> "알 수 없음";
        };
    }

    /* 필드용 이름 변환 */
    public String organizationName(Long id) {
        return id == null ? "-" :
                organizationRepository.findById(id)
                        .map(o -> o.getName())
                        .orElse("알 수 없음");
    }

    public String orgCategoryName(Long id) {
        return id == null ? "-" :
                orgCategoryRepository.findById(id)
                        .map(c -> c.getName())
                        .orElse("알 수 없음");
    }

    public String positionName(Long id) {
        return id == null ? "-" :
                positionRepository.findById(id)
                        .map(p -> p.getName())
                        .orElse("알 수 없음");
    }

    public String rankName(Long id) {
        return id == null ? "-" :
                rankRepository.findById(id)
                        .map(r -> r.getName())
                        .orElse("알 수 없음");
    }

    public String enumDisplay(String key, Object value) {
        if (value == null) return "-";

        return switch (key) {
            case "status" -> switch (value.toString()) {
                case "ACTIVE" -> "재직";
                case "SUSPENDED" -> "휴직";
                case "RESIGNED" -> "퇴사";
                case "TEMP" -> "임시계정";
                default -> value.toString();
            };

            case "employmentType" -> switch (value.toString()) {
                case "REGULAR" -> "정규직";
                case "NON_REGULAR" -> "비정규직";
                case "CONTRACT" -> "계약직";
                default -> value.toString();
            };

            case "gender" -> switch (value.toString()) {
                case "MALE" -> "남성";
                case "FEMALE" -> "여성";
                default -> value.toString();
            };

            case "role" -> switch (value.toString()) {
                case "ADMIN" -> "관리자";
                case "EMPLOYEE" -> "사원";
                case "COMPANY_ADMIN" -> "대표 관리자";
                default -> value.toString();
            };

            default -> value.toString();
        };
    }

    public String booleanDisplay(String key, Object value) {
        if (value == null) return "-";

        if ("isHead".equals(key)) {
            return Boolean.parseBoolean(value.toString())
                    ? "가능"
                    : "불가";
        }

        return value.toString();
    }

}
