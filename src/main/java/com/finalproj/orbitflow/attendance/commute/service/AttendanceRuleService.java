package com.finalproj.orbitflow.attendance.commute.service;

import com.finalproj.orbitflow.attendance.commute.dto.AttendanceRuleDto;
import com.finalproj.orbitflow.attendance.commute.dto.EmployeeAttRuleDto;
import com.finalproj.orbitflow.attendance.commute.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.commute.entity.EmployeeAttRule;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.attendance.commute.repository.EmployeeAttRuleRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceRuleService {

    private final EmployeeAttRuleRepository employeeRuleRepository;
    private final AttendanceRuleRepository defaultRuleRepository;
    private final EmployeeRepository employeeRepository;

    // TODO: 실제 구현 시 Spring Security 등을 통해 현재 로그인한 관리자 ID와 회사 ID를 가져와야 함
    private final Long CURRENT_COMPANY_ID = 1L;
    private final Long CURRENT_ADMIN_ID = 99L;

    // 회사 기본 규칙 (9시-18시, 60분 휴게)
    private final LocalTime DEFAULT_START = LocalTime.of(9, 0);
    private final LocalTime DEFAULT_END = LocalTime.of(18, 0);
    private final Integer DEFAULT_BREAK = 60;


    // =======================================================
    // I. 회사 기본 규칙 (Default Rule) 관리
    // =======================================================

    /**
     * 1. 기본 규칙 조회 (GET /rules/default)
     */
    public AttendanceRuleDto.AttendanceRuleResponse getDefaultRule() {
        AttendanceRule rule = defaultRuleRepository.findByCompanyIdAndIsDefaultTrue(CURRENT_COMPANY_ID)
                .orElseGet(() -> createInitialDefaultRule(CURRENT_COMPANY_ID));

        return new AttendanceRuleDto.AttendanceRuleResponse(rule);
    }

    /**
     * 기본 규칙이 없을 경우 초기 9시~18시 규칙을 생성
     */
    @Transactional
    public AttendanceRule createInitialDefaultRule(Long companyId) {
        AttendanceRule newRule = new AttendanceRule();
        newRule.setCompanyId(companyId);
        newRule.setDefaultStartTime(DEFAULT_START);
        newRule.setDefaultEndTime(DEFAULT_END);
        newRule.setDefaultBreakMinutes(DEFAULT_BREAK);
        newRule.setIsDefault(true);
        return defaultRuleRepository.save(newRule);
    }

    /**
     * 2. 기본 규칙 수정 (PUT /rules/default)
     */
    @Transactional
    public AttendanceRuleDto.AttendanceRuleResponse updateDefaultRule(AttendanceRuleDto.AttendanceRuleUpdateRequest request) {
        AttendanceRule rule = defaultRuleRepository.findByCompanyIdAndIsDefaultTrue(CURRENT_COMPANY_ID)
                .orElseThrow(() -> new IllegalStateException("기본 규칙이 초기화되지 않았습니다."));

        rule.updateRule(
                request.defaultStartTime(),
                request.defaultEndTime(),
                request.defaultBreakMinutes()
        );

        return new AttendanceRuleDto.AttendanceRuleResponse(rule);
    }

    // =======================================================
    // II. 사원별 예외 규칙 (Exception Rule) 관리
    // =======================================================

    /**
     * 3. 예외 규칙 목록 조회 (GET /rules/exception)
     */
    public List<EmployeeAttRuleDto.EmployeeAttRuleResponse> getExceptionRules() {
        // 1. 모든 예외 규칙 레코드를 가져옵니다.
        List<EmployeeAttRule> rules = employeeRuleRepository.findAllByCompanyId(CURRENT_COMPANY_ID);

        if (rules.isEmpty()) {
            return List.of();
        }

        // 2. [N+1 방지] employeeId 목록 추출
        List<Long> employeeIds = rules.stream()
                .map(EmployeeAttRule::getEmployeeId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 한 번의 쿼리로 해당 ID의 모든 사원 정보를 가져와 Map<ID, Name>으로 변환합니다.
        Map<Long, String> employeeMap = employeeRepository.findAllByIdIn(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        // 4. 규칙 목록을 순회하며 DTO를 생성하고 이름을 매핑합니다.
        return rules.stream()
                .map(rule -> {
                    String name = employeeMap.getOrDefault(rule.getEmployeeId(), "이름 미확인");
                    return new EmployeeAttRuleDto.EmployeeAttRuleResponse(rule, name);
                })
                .collect(Collectors.toList());
    }

    /**
     * 4. 예외 규칙 상세 조회 (GET /rules/exception/{ruleId})
     * - 상세 조회는 이름을 포함하지 않는 간단한 DTO를 반환해도 됩니다. (필요 시 수정 가능)
     */
    public EmployeeAttRuleDto.EmployeeAttRuleResponse getExceptionRuleDetail(Long ruleId) {
        EmployeeAttRule rule = employeeRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 규칙 ID입니다: " + ruleId));

        if (!rule.getCompanyId().equals(CURRENT_COMPANY_ID)) {
            throw new SecurityException("해당 회사에 속하지 않은 규칙에 접근할 수 없습니다.");
        }

        return new EmployeeAttRuleDto.EmployeeAttRuleResponse(rule);
    }

    @Transactional
    public EmployeeAttRuleDto.EmployeeAttRuleResponse createExceptionRule(EmployeeAttRuleDto.EmployeeAttRuleCreateRequest request) {
        // 사원 존재 여부 및 회사 일치 여부 검증 추가 (권장)
        Employee targetEmployee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사원입니다."));

        EmployeeAttRule newRule = new EmployeeAttRule();
        newRule.setCompanyId(CURRENT_COMPANY_ID);
        newRule.setEmployeeId(request.employeeId());
        newRule.setStartTime(request.startTime());
        newRule.setEndTime(request.endTime());
        newRule.setBreakMinutes(request.breakMinutes());
        newRule.setReason(request.reason());
        newRule.setValidFrom(request.validFrom());
        newRule.setValidTo(request.validTo());
        newRule.setAppliedAt(LocalDateTime.now());


        // [에러 해결] isActive가 null로 저장되지 않도록 보장
        newRule.setIsActive(true);

        EmployeeAttRule savedRule = employeeRuleRepository.save(newRule);
        return new EmployeeAttRuleDto.EmployeeAttRuleResponse(savedRule);
    }

    /**
     * 6. 예외 규칙 수정 (PUT /rules/exception/{ruleId})
     */
    @Transactional
    public EmployeeAttRuleDto.EmployeeAttRuleResponse updateExceptionRule(Long ruleId, EmployeeAttRuleDto.EmployeeAttRuleUpdateRequest request) {
        EmployeeAttRule rule = employeeRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 규칙 ID입니다: " + ruleId));

        if (!rule.getCompanyId().equals(CURRENT_COMPANY_ID)) {
            throw new SecurityException("해당 회사에 속하지 않은 규칙은 수정할 수 없습니다.");
        }

        // [에러 해결] DTO에서 isActive가 null로 넘어올 경우를 대비한 방어 로직
        Boolean activeStatus = request.isActive();
        if (activeStatus == null) {
            activeStatus = rule.getIsActive() != null ? rule.getIsActive() : true;
        }

        rule.updateRule(
                request.startTime(),
                request.endTime(),
                request.breakMinutes(),
                request.reason(),
                request.validFrom(),
                request.validTo(),
                activeStatus // null이 아닌 값이 전달되도록 보장
        );

        rule.setAppliedAt(LocalDateTime.now());
        // 수정 시에도 누가 수정했는지 남기고 싶다면 추가
        // rule.setAppliedBy(CURRENT_ADMIN_ID);

        return new EmployeeAttRuleDto.EmployeeAttRuleResponse(rule);
    }


    /**
     * 7. 예외 규칙 삭제 (DELETE /rules/exception/{ruleId})
     */
    @Transactional
    public void deleteExceptionRule(Long ruleId) {
        // ... (생략: 기존 로직과 동일) ...
        EmployeeAttRule rule = employeeRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 규칙 ID입니다: " + ruleId));

        if (!rule.getCompanyId().equals(CURRENT_COMPANY_ID)) {
            throw new SecurityException("해당 회사에 속하지 않은 규칙은 삭제할 수 없습니다.");
        }

        employeeRuleRepository.delete(rule);
    }
}