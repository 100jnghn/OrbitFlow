package com.finalproj.orbitflow.hr.employee.service;

import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.dto.*;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.logAudit.dto.AuditLogResDto;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEntityType;
import com.finalproj.orbitflow.hr.logAudit.enums.AuditEventType;
import com.finalproj.orbitflow.hr.logAudit.service.AuditLogService;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import com.finalproj.orbitflow.hr.positionCategory.repository.PositionCategoryRepository;
import com.finalproj.orbitflow.hr.rank.entity.HrRank;
import com.finalproj.orbitflow.hr.rank.repository.RankRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeService
 * @since : 2025-12-23 화요일
 */

@Service
@Transactional
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final OrgRepository orgRepository;
    private final RankRepository rankRepository;
    private final PositionCategoryRepository positionCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    /* =============================
       조회
       ============================= */

    @Transactional(readOnly = true)
    public List<EmployeeResDto> findByOrgAndPosition(
            Long companyId,
            Long orgId,
            Long positionCategoryId
    ) {
        return employeeRepository
                .findByCompany_IdAndOrganization_IdAndPositionCategory_IdAndStatus(
                        companyId,
                        orgId,
                        positionCategoryId,
                        EmployeeStatus.ACTIVE
                )
                .stream()
                .map(EmployeeResDto::from)
                .toList();
    }


    /* =============================
       목록 조회
       ============================= */
    @Transactional(readOnly = true)
    public Page<EmployeeListResDto> search(
            Long companyId,
            String keyword,
            EmployeeStatus status,
            Pageable pageable
    ) {
        return employeeRepository
                .searchAdmin(companyId, keyword, status, pageable)
                .map(e -> new EmployeeListResDto(
                        e.getId(),
                        e.getName(),
                        e.getEmail(),
                        buildOrgPath(e.getOrganization()),
                        e.getRank() != null ? e.getRank().getName() : null,
                        e.getPositionCategory() != null ? e.getPositionCategory().getName() : null,
                        e.getStatus()
                ));
    }


    /* =============================
       상세 조회
       ============================= */
    @Transactional(readOnly = true)
    public EmployeeDetailResDto getDetail(Long companyId, Long employeeId) {

        Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다."));

        if (!e.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("다른 회사의 사원입니다.");
        }

        return new EmployeeDetailResDto(
                e.getId(),
                e.getName(),
                e.getEmail(),
                e.getEmployeeNo(),
                e.getGender(),
                e.getBirthDate(),
                e.getPhone(),
                e.getInternalPhone(),
                e.getHireDate(),
                e.getEmploymentType(),
                e.getStatus(),
                buildOrgPath(e.getOrganization()),
                e.getRank() != null ? e.getRank().getName() : null,
                e.getPositionCategory() != null ? e.getPositionCategory().getName() : null
        );
    }


    /* =============================
       생성
       ============================= */

    public void create(Long companyId, EmployeeCreateReqDto dto) {

        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("회사 정보가 없습니다."));

        Organization organization = orgRepository.findById(dto.getOrgId())
                .orElseThrow(() -> new IllegalArgumentException("조직 정보가 없습니다."));

        HrRank rank = dto.getRankId() != null
                ? rankRepository.findById(dto.getRankId()).orElse(null)
                : null;

        PositionCategory positionCategory = dto.getPositionCategoryId() != null
                ? positionCategoryRepository.findById(dto.getPositionCategoryId()).orElse(null)
                : null;

        String tempPassword = UUID.randomUUID().toString().substring(0, 10);

        Employee employee = Employee.create(
                company,
                organization,
                rank,
                positionCategory,
                dto,
                passwordEncoder.encode(tempPassword)
        );

        Employee saved = employeeRepository.save(employee);

        Employee actor = getActorEmployee();


        auditLogService.log(
                company,
                actor,
                AuditEntityType.EMPLOYEE,
                saved.getId(),
                AuditEventType.CREATE,
                null,
                """
                        {
                          "name": "%s",
                          "email": "%s",
                          "employeeNo": "%s"
                        }
                        """.formatted(
                        saved.getName(),
                        saved.getEmail(),
                        saved.getEmployeeNo()
                )
        );


        // TODO: 메일 발송은 추후
    }



    /* =============================
       수정
       ============================= */

    public void update(Long companyId, Long employeeId, EmployeeUpdateReqDto dto) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다."));

        String before = """
                {
                  "orgId": %d,
                  "rankId": %s,
                  "positionCategoryId": %s,
                  "status": "%s"
                }
                """.formatted(
                employee.getOrganization().getId(),
                employee.getRank() != null ? employee.getRank().getId() : null,
                employee.getPositionCategory() != null ? employee.getPositionCategory().getId() : null,
                employee.getStatus()
        );


        if (!employee.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("다른 회사의 사원입니다.");
        }

        if (dto.getOrgId() != null) {
            employee.changeOrganization(
                    orgRepository.findById(dto.getOrgId())
                            .orElseThrow(() -> new IllegalArgumentException("조직 정보가 없습니다."))
            );
        }

        if (dto.getRankId() != null) {
            employee.changeRank(rankRepository.findById(dto.getRankId()).orElse(null));
        }

        if (dto.getPositionCategoryId() != null) {
            employee.changePosition(
                    positionCategoryRepository.findById(dto.getPositionCategoryId()).orElse(null)
            );
        }

        if (dto.getEmploymentType() != null) {
            employee.changeEmploymentType(dto.getEmploymentType());
        }


        if (dto.getRole() != null) {
            employee.changeRole(dto.getRole());
        }

        if (dto.getStatus() != null) {
            handleStatusChange(employee, dto.getStatus());
        }


        auditLogService.log(
                employee.getCompany(),
                getActorEmployee(),
                AuditEntityType.EMPLOYEE,
                employee.getId(),
                AuditEventType.UPDATE,
                before,
                """
                        {
                          "orgId": %s,
                          "rankId": %s,
                          "positionCategoryId": %s,
                          "status": "%s"
                        }
                        """.formatted(
                        employee.getOrganization().getId(),
                        employee.getRank() != null ? employee.getRank().getId() : null,
                        employee.getPositionCategory() != null ? employee.getPositionCategory().getId() : null,
                        employee.getStatus()
                )
        );

    }


    /* =============================
       상태 처리
       ============================= */

    private void handleStatusChange(Employee employee, EmployeeStatus newStatus) {
//        if (employee.getStatus() == newStatus) return;
//
//        switch (newStatus) {
//            case ACTIVE -> employee.activate();
//            case SUSPENDED -> employee.suspend();
//            case RESIGNED -> processResignation(employee);
//            default -> throw new IllegalStateException("지원하지 않는 상태 변경");
//        }

        EmployeeStatus before = employee.getStatus();
        if (before == newStatus) return;

        switch (newStatus) {
            case ACTIVE -> employee.activate();
            case SUSPENDED -> employee.suspend();
            case RESIGNED -> processResignation(employee);
        }

        auditLogService.log(
                employee.getCompany(),
                getActorEmployee(),
                AuditEntityType.EMPLOYEE,
                employee.getId(),
                AuditEventType.STATUS_CHANGE,
                "\"" + before + "\"",
                "\"" + newStatus + "\""
        );

    }

    private void processResignation(Employee employee) {
        employee.resign();
        employee.changeEmail("resigned_" + employee.getId() + "@orbitflow.local");
        employee.changePassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        employee.clearContactInfo();
    }


    /* =============================
       조직 path 계산
       ============================= */

    private String buildOrgPath(Organization org) {
        Deque<String> names = new ArrayDeque<>();
        Organization cur = org;

        while (cur != null) {
            names.push(cur.getName());
            if (cur.getParentOrgId() == null) break;
            cur = orgRepository.findById(cur.getParentOrgId()).orElse(null);
        }

        return String.join(" > ", names);
    }


    /* =============================
       공통 유틸 메서드
       ============================= */
    private Employee getActorEmployee() {
        Long actorId = SecurityUtils.getEmployeeId();

        return employeeRepository.findById(actorId)
                .orElseThrow(() -> new IllegalStateException("로그인 사원 정보를 찾을 수 없습니다."));
    }



    @Transactional(readOnly = true)
    public List<AuditLogResDto> getEmployeeAuditLogs(Long companyId, Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다."));

        if (!employee.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("다른 회사의 사원입니다.");
        }

        return auditLogService.findEmployeeLogs(employeeId)
                .stream()
                .map(AuditLogResDto::from)
                .toList();
    }

}
