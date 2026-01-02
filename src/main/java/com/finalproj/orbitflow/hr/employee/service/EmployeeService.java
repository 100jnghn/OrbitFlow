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

import java.util.*;

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

                // 표시용
                buildOrgPath(e.getOrganization()),
                e.getRank() != null ? e.getRank().getName() : null,
                e.getPositionCategory() != null ? e.getPositionCategory().getName() : null,

                // 편집용
                e.getOrganization().getId(),
                e.getRank() != null ? e.getRank().getId() : null,
                e.getPositionCategory() != null ? e.getPositionCategory().getId() : null
        );
    }


    /* =============================
       생성 (TEMP 강제)
       ============================= */

    public void create(Long companyId, EmployeeCreateReqDto dto) {

        // 이메일 중복
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

        // 임시 비번 발급 (메일 전송은 TODO)
        String tempPassword = UUID.randomUUID().toString().substring(0, 10);

        // status는 무조건 TEMP로 엔티티에서 강제 설정
        Employee employee = Employee.create(
                company,
                organization,
                rank,
                positionCategory,
                dto,
                passwordEncoder.encode(tempPassword)
        );

        Employee saved = employeeRepository.save(employee);

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("name", saved.getName());
        after.put("email", saved.getEmail());
        after.put("employeeNo", saved.getEmployeeNo());
        after.put("orgId", organization.getId());
        after.put("rankId", rank != null ? rank.getId() : null);
        after.put("positionCategoryId", positionCategory != null ? positionCategory.getId() : null);
        after.put("hireDate", saved.getHireDate().toString());
        after.put("gender", saved.getGender().name());
        after.put("employmentType", saved.getEmploymentType().name());
        after.put("role", saved.getRole().name());
        after.put("status", saved.getStatus().name()); // TEMP

        auditLogService.log(
                company,
                getActorEmployee(),
                AuditEntityType.EMPLOYEE,
                saved.getId(),
                AuditEventType.CREATE,
                null,
                after
        );
    }



    /* =============================
       수정 (변경된 필드만 before/after 기록)
       - status는 여기서 처리하지 않음
       ============================= */

    public void update(Long companyId, Long employeeId, EmployeeUpdateReqDto dto) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다."));
        if (!employee.getCompany().getId().equals(companyId)) throw new IllegalStateException("다른 회사의 사원입니다.");

        Map<String, Object> before = new LinkedHashMap<>();
        Map<String, Object> after = new LinkedHashMap<>();

        // 문자열: "" 들어오면 null로 간주(프론트에서 trim/empty 처리해도 좋지만 방어)
        String name = normalizeStr(dto.getName());
        String phone = normalizeStr(dto.getPhone());
        String internalPhone = normalizeStr(dto.getInternalPhone());

        // ===== 기본 정보 =====
        if (name != null && !name.equals(employee.getName())) {
            putDiff(before, after, "name", employee.getName(), name);
            employee.updateBasicInfo(name, null, null, null);
        }
        if (!Objects.equals(phone, employee.getPhone())) {
            // phone은 null 허용이라 Objects.equals로 비교
            putDiff(before, after, "phone", employee.getPhone(), phone);
            employee.updateBasicInfo(null, phone, null, null);
        }
        if (!Objects.equals(internalPhone, employee.getInternalPhone())) {
            putDiff(before, after, "internalPhone", employee.getInternalPhone(), internalPhone);
            employee.updateBasicInfo(null, null, internalPhone, null);
        }
        if (dto.getBirthDate() != null && !dto.getBirthDate().equals(employee.getBirthDate())) {
            putDiff(before, after, "birthDate",
                    employee.getBirthDate() != null ? employee.getBirthDate().toString() : null,
                    dto.getBirthDate() != null ? dto.getBirthDate().toString() : null);
            employee.updateBasicInfo(null, null, null, dto.getBirthDate());
        }

        // ===== 조직/직급/직책 =====
        if (dto.getOrgId() != null && !dto.getOrgId().equals(employee.getOrganization().getId())) {
            putDiff(before, after, "orgId", employee.getOrganization().getId(), dto.getOrgId());
            Organization org = orgRepository.findById(dto.getOrgId())
                    .orElseThrow(() -> new IllegalArgumentException("조직 정보가 없습니다."));
            employee.changeOrganization(org);
        }

        if (dto.getRankId() != null) {
            Long currentRankId = employee.getRank() != null ? employee.getRank().getId() : null;
            if (!dto.getRankId().equals(currentRankId)) {
                putDiff(before, after, "rankId", currentRankId, dto.getRankId());
                employee.changeRank(rankRepository.findById(dto.getRankId()).orElse(null));
            }
        }

        if (dto.getPositionCategoryId() != null) {
            Long currentPosId = employee.getPositionCategory() != null ? employee.getPositionCategory().getId() : null;
            if (!dto.getPositionCategoryId().equals(currentPosId)) {
                putDiff(before, after, "positionCategoryId", currentPosId, dto.getPositionCategoryId());
                employee.changePosition(positionCategoryRepository.findById(dto.getPositionCategoryId()).orElse(null));
            }
        }

        // ===== 고용/권한 =====
        if (dto.getEmploymentType() != null && dto.getEmploymentType() != employee.getEmploymentType()) {
            putDiff(
                    before,
                    after,
                    "employmentType",
                    employee.getEmploymentType() != null ? employee.getEmploymentType().name() : null,
                    dto.getEmploymentType() != null ? dto.getEmploymentType().name() : null
            );
            employee.changeEmploymentType(dto.getEmploymentType());
        }

        if (dto.getRole() != null && dto.getRole() != employee.getRole()) {
            putDiff(before, after, "role", employee.getRole(), dto.getRole());
            employee.changeRole(dto.getRole());
        }

        // status는 update에서 절대 처리 X
        // dto.getStatus() 무시(권장: DTO에서 제거)

        if (!after.isEmpty()) {
            auditLogService.log(
                    employee.getCompany(),
                    getActorEmployee(),
                    AuditEntityType.EMPLOYEE,
                    employee.getId(),
                    AuditEventType.UPDATE,
                    before,
                    after
            );
        }
    }



    /* =============================
       이메일 인증을 통한 계정 활성화 (TEMP → ACTIVE)
       ============================= */
    public void activate(Employee employee) {

        if (employee.getStatus() != EmployeeStatus.TEMP) {
            throw new IllegalStateException("TEMP 계정만 활성화할 수 있습니다.");
        }

        employee.activate();

        auditLogService.log(
                employee.getCompany(),
                employee, // 본인 인증
                AuditEntityType.EMPLOYEE,
                employee.getId(),
                AuditEventType.ACTIVATE,
                Map.of("status", EmployeeStatus.TEMP),
                Map.of("status", EmployeeStatus.ACTIVE)
        );
    }


    /* =============================
       상태 변경 (별도 API)
       ============================= */

    public void updateStatus(Long companyId, Long employeeId, EmployeeStatus status) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("사원 없음"));
        if (!employee.getCompany().getId().equals(companyId)) throw new IllegalStateException("회사 불일치");
        handleStatusChange(employee, status);
    }


    private void handleStatusChange(Employee employee, EmployeeStatus newStatus) {

        EmployeeStatus current = employee.getStatus();

        // RESIGNED는 어떤 경우에도 변경 불가
        if (current == EmployeeStatus.RESIGNED) {
            throw new IllegalStateException("퇴사한 사원은 상태를 변경할 수 없습니다.");
        }

        // TEMP는 퇴사만 가능 (ACTIVE는 이메일 인증 전용 API)
        if (current == EmployeeStatus.TEMP && newStatus != EmployeeStatus.RESIGNED) {
            throw new IllegalStateException("임시 계정은 이메일 인증을 통해서만 활성화할 수 있습니다.");
        }

        if (current == newStatus) return;

        switch (newStatus) {
            case ACTIVE -> throw new IllegalStateException("ACTIVE는 이메일 인증 전용");
            case SUSPENDED -> employee.suspend();
            case RESIGNED -> processResignation(employee);
        }

        auditLogService.log(
                employee.getCompany(),
                getActorEmployee(),
                AuditEntityType.EMPLOYEE,
                employee.getId(),
                AuditEventType.STATUS_CHANGE,
                Map.of("status", current),
                Map.of("status", newStatus)
        );
    }

    private void processResignation(Employee employee) {
        employee.resign();
        employee.changeEmail("resigned_" + employee.getId() + "@orbitflow.local");
        employee.changePassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        employee.clearContactInfo();
    }


    /* =============================
       Audit Logs
       ============================= */
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

    @Transactional(readOnly = true)
    public List<EmployeeSearchDto> searchEmployees(Long companyId, String keyword) {
        return employeeRepository.searchByCompanyIdAndKeyword(companyId, keyword).stream()
                .map(EmployeeSearchDto::from)
                .toList();
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


    private void putDiff(Map<String, Object> before, Map<String, Object> after, String key, Object b, Object a) {
        before.put(key, b);
        after.put(key, a);
    }

    private String normalizeStr(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Transactional(readOnly = true)
    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사원이 없습니다."));
    }

    /**
     * 비밀번호 재설정
     */
    public void resetPassword(Employee employee, String rawPassword) {

        String encoded = passwordEncoder.encode(rawPassword);
        employee.changePassword(encoded);

        // ※ Audit은 Controller에서 이미 하고 있으므로 여기선 안 함
    }

    @Transactional(readOnly = true)
    public EmployeeUpdateResDto getEditView(Long companyId, Long employeeId) {
        Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("사원을 찾을 수 없습니다."));

        if (!e.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("다른 회사의 사원입니다.");
        }

        return EmployeeUpdateResDto.from(e);
    }

}
