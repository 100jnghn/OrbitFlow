package com.finalproj.orbitflow.attendance.default_rule.service;

import com.finalproj.orbitflow.attendance.default_rule.dto.AttRuleResDto;
import com.finalproj.orbitflow.attendance.default_rule.dto.AttRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.default_rule.dto.EmployeeSearchDto;
import com.finalproj.orbitflow.attendance.exception_rule.dto.EmpAttRuleCreateReqDto;
import com.finalproj.orbitflow.attendance.exception_rule.dto.EmpAttRuleResDto;
import com.finalproj.orbitflow.attendance.exception_rule.dto.EmpAttRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.default_rule.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.exception_rule.entity.EmployeeAttRule;
import com.finalproj.orbitflow.attendance.default_rule.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.attendance.exception_rule.repository.EmployeeAttRuleRepository;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    // =======================================================
    // I. 회사 기본 규칙 (Default Rule)
    // =======================================================

    public AttRuleResDto getDefaultRule(Long companyId) {
        return new AttRuleResDto(findDefaultRuleOrThrow(companyId));
    }

    @Transactional
    public AttRuleResDto updateDefaultRule(Long companyId, AttRuleUpdateReqDto request) {
        AttendanceRule rule = findDefaultRuleOrThrow(companyId);

        rule.updateRule(
                request.defaultStartTime(),
                request.defaultEndTime(),
                request.defaultBreakMinutes()
        );

        return new AttRuleResDto(rule);
    }

    // =======================================================
    // II. 사원별 예외 규칙 (Exception Rule)
    // =======================================================

    public List<EmpAttRuleResDto> getExceptionRules(Long companyId) {
        List<EmployeeAttRule> rules = employeeRuleRepository.findByCompanyId(companyId);

        // N+1 방지를 위한 Bulk 조회 및 Map 변환
        Map<Long, String> employeeMap = getEmployeeNameMap(rules);

        return rules.stream()
                .map(rule -> new EmpAttRuleResDto(rule, employeeMap.getOrDefault(rule.getEmployeeId(), "이름 미확인")))
                .toList();
    }

    public EmpAttRuleResDto getExceptionRuleDetail(Long companyId, Long ruleId) {
        EmployeeAttRule rule = findExceptionRuleOrThrow(ruleId);
        validateCompanyAccess(companyId, rule.getCompanyId());

        return new EmpAttRuleResDto(rule, getEmployeeName(rule.getEmployeeId()));
    }

    @Transactional
    public EmpAttRuleResDto createExceptionRule(SecurityUser admin, EmpAttRuleCreateReqDto request) {
        Employee employee = findEmployeeOrThrow(request.employeeId());
        validateCompanyAccess(admin.getCompanyId(), employee.getCompany().getId());

        EmployeeAttRule rule = EmployeeAttRule.builder()
                .companyId(admin.getCompanyId())
                .employeeId(request.employeeId())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .breakMinutes(request.breakMinutes())
                .reason(request.reason())
                .validFrom(request.validFrom())
                .validTo(request.validTo())
                .isActive(true)
                .appliedAt(LocalDateTime.now())
                .build();

        return new EmpAttRuleResDto(employeeRuleRepository.save(rule), employee.getName());
    }

    @Transactional
    public EmpAttRuleResDto updateExceptionRule(Long companyId, Long ruleId, EmpAttRuleUpdateReqDto request) {
        EmployeeAttRule rule = findExceptionRuleOrThrow(ruleId);
        validateCompanyAccess(companyId, rule.getCompanyId());

        rule.updateRule(
                request.startTime(),
                request.endTime(),
                request.breakMinutes(),
                request.reason(),
                request.validFrom(),
                request.validTo(),
                request.isActive() != null ? request.isActive() : rule.getIsActive()
        );

        rule.setAppliedAt(LocalDateTime.now());

        return new EmpAttRuleResDto(rule, getEmployeeName(rule.getEmployeeId()));
    }

    @Transactional
    public void deleteExceptionRule(SecurityUser admin, Long ruleId) {
        EmployeeAttRule rule = findExceptionRuleOrThrow(ruleId);
        validateCompanyAccess(admin.getCompanyId(), rule.getCompanyId());

        employeeRuleRepository.delete(rule);
    }

    public List<EmployeeSearchDto> searchEmployees(Long companyId, String keyword) {
        return employeeRepository.searchByCompanyIdAndKeyword(companyId, keyword).stream()
                .map(EmployeeSearchDto::from)
                .collect(Collectors.toList());
    }

    // =======================================================
    // Private Helper Methods (추출된 공통 로직)
    // =======================================================

    private AttendanceRule findDefaultRuleOrThrow(Long companyId) {
        return defaultRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회사의 기본 규칙을 찾을 수 없습니다."));
    }

    private EmployeeAttRule findExceptionRuleOrThrow(Long ruleId) {
        return employeeRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 규칙입니다. ID: " + ruleId));
    }

    private Employee findEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("사원 정보를 찾을 수 없습니다. ID: " + employeeId));
    }

    private void validateCompanyAccess(Long adminCompanyId, Long targetCompanyId) {
        if (!adminCompanyId.equals(targetCompanyId)) {
            throw new SecurityException("해당 데이터에 대한 접근 권한이 없습니다.");
        }
    }

    private String getEmployeeName(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .map(Employee::getName)
                .orElse("알 수 없음");
    }

    private Map<Long, String> getEmployeeNameMap(List<EmployeeAttRule> rules) {
        List<Long> employeeIds = rules.stream().map(EmployeeAttRule::getEmployeeId).toList();
        return employeeRepository.findAllByIdIn(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));
    }
}