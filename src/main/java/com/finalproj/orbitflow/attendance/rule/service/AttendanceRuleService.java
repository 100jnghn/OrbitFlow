package com.finalproj.orbitflow.attendance.rule.service;

import com.finalproj.orbitflow.attendance.rule.dto.response.DefaultRuleResDto;
import com.finalproj.orbitflow.attendance.rule.dto.request.DefaultRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.rule.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.rule.entity.EmployeeRule;
import com.finalproj.orbitflow.attendance.rule.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.attendance.rule.dto.request.EmpAttRuleCreateReqDto;
import com.finalproj.orbitflow.attendance.rule.dto.response.EmployeeRuleResDto;
import com.finalproj.orbitflow.attendance.rule.dto.request.EmpAttRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.rule.repository.EmployeeRuleRepository;
import com.finalproj.orbitflow.global.exception.BusinessException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceRuleService {

    private final EmployeeRuleRepository employeeRuleRepository;
    private final AttendanceRuleRepository defaultRuleRepository;
    private final EmployeeRepository employeeRepository;


    public DefaultRuleResDto getDefaultRule(Long companyId) {
        return new DefaultRuleResDto(findDefaultRuleOrThrow(companyId));
    }

    @Transactional
    public DefaultRuleResDto updateDefaultRule(Long companyId, DefaultRuleUpdateReqDto request) {
        AttendanceRule rule = findDefaultRuleOrThrow(companyId);
        rule.updateRule(request.defaultStartTime(), request.defaultEndTime(), request.defaultBreakMinutes());
        return new DefaultRuleResDto(rule);
    }


    public List<EmployeeRuleResDto> getExceptionRules(Long companyId) {
        List<EmployeeRule> rules = employeeRuleRepository.findByCompanyIdAndIsActiveTrueOrderByAppliedAtDesc(companyId);

        Map<Long, Employee> employeeMap = getEmployeeMap(rules);

        return rules.stream()
                .map(rule -> toEmployeeRuleResDto(rule, employeeMap.get(rule.getEmployeeId())))
                .toList();
    }

    @Transactional
    public EmployeeRuleResDto createExceptionRule(Long companyId, EmpAttRuleCreateReqDto request) {
        validateRuleDates(request.validFrom(), request.validTo());

        Employee employee = findEmployeeOrThrow(request.employeeId());
        validateCompanyAccess(companyId, employee.getCompany().getId());

        EmployeeRule rule = EmployeeRule.builder()
                .companyId(companyId)
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

        return toEmployeeRuleResDto(employeeRuleRepository.save(rule), employee);
    }


    public EmployeeRuleResDto getExceptionRuleDetail(Long companyId, Long ruleId) {
        EmployeeRule rule = findExceptionRuleOrThrow(ruleId);
        validateCompanyAccess(companyId, rule.getCompanyId());

        Employee employee = findEmployeeOrThrow(rule.getEmployeeId());

        return toEmployeeRuleResDto(rule, employee);
    }


    @Transactional
    public EmployeeRuleResDto updateExceptionRule(Long companyId, Long ruleId, EmpAttRuleUpdateReqDto request) {
        EmployeeRule rule = findExceptionRuleOrThrow(ruleId);
        validateCompanyAccess(companyId, rule.getCompanyId());

        rule.updateRule(
                request.startTime(),
                request.endTime(),
                request.breakMinutes(),
                request.reason(),
                request.validFrom(),
                request.validTo(),
                request.isActive()
        );

        Employee employee = findEmployeeOrThrow(rule.getEmployeeId());
        return toEmployeeRuleResDto(rule, employee);
    }


    @Transactional
    public void deleteExceptionRule(Long companyId, Long ruleId) {
        EmployeeRule rule = findExceptionRuleOrThrow(ruleId);
        validateCompanyAccess(companyId, rule.getCompanyId());

        rule.delete();
    }


    private void validateRuleDates(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new BusinessException("종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    private EmployeeRuleResDto toEmployeeRuleResDto(EmployeeRule rule, Employee employee) {
        return new EmployeeRuleResDto(
                rule,
                employee != null ? employee.getName() : "이름 미확인",
                employee != null ? employee.getEmployeeNo() : null
        );
    }

    private AttendanceRule findDefaultRuleOrThrow(Long companyId) {
        return defaultRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
                .orElseThrow(() -> new BusinessException("기본 근태 규칙이 설정되지 않은 회사입니다."));
    }

    private EmployeeRule findExceptionRuleOrThrow(Long ruleId) {
        return employeeRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException("해당 예외 규칙을 찾을 수 없습니다."));
    }

    private Employee findEmployeeOrThrow(Long employeeId) {
        if (employeeId == null) {
            throw new BusinessException("사원 식별 번호가 누락되었습니다.");
        }
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("사원 정보를 찾을 수 없습니다."));
    }

    private void validateCompanyAccess(Long currentCid, Long targetCid) {
        if (!currentCid.equals(targetCid)) {
            throw new BusinessException("해당 데이터에 접근할 권한이 없습니다.");
        }
    }

    private Map<Long, Employee> getEmployeeMap(List<EmployeeRule> rules) {
        List<Long> employeeIds = rules.stream()
                .map(EmployeeRule::getEmployeeId)
                .distinct()
                .toList();

        return employeeRepository.findAllByIdIn(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, emp -> emp));
    }
}