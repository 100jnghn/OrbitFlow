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
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.notification.enums.NotificationType;
import com.finalproj.orbitflow.notification.repository.NotificationRepository;
import com.finalproj.orbitflow.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceRuleService {

    private final EmployeeRuleRepository employeeRuleRepository;
    private final AttendanceRuleRepository defaultRuleRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationCommandService notificationService;
    private final AttendanceRuleRepository attendanceRuleRepository;

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

        EmployeeRule savedRule = employeeRuleRepository.save(rule);

        notificationService.createNotification(
                companyId,
                employee.getId(),
                NotificationType.ATTENDANCE,
                "새로운 근태 예외 규칙이 적용되었습니다. 적용 시작일: " + request.validFrom(),
                "/");

        return toEmployeeRuleResDto(savedRule, employee);
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
                request.startTime(), request.endTime(), request.breakMinutes(),
                request.reason(), request.validFrom(), request.validTo(), request.isActive());

        Employee employee = findEmployeeOrThrow(rule.getEmployeeId());

        notificationService.createNotification(
                companyId,
                employee.getId(),
                NotificationType.ATTENDANCE,
                "적용 중인 근태 예외 규칙이 수정되었습니다.",
                "/");

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
                employee != null ? employee.getEmployeeNo() : null);
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


    /**
     * 새로운 회사를 위한 기본 근태 규칙(09:00 - 18:00)을 생성합니다.
     * @param company 신규 생성된 회사 엔티티
     */
    @Transactional
    public void createDefaultAttendanceRule(Company company) {
        // 중복 생성 방지 체크
        if (attendanceRuleRepository.existsByCompanyId(company.getId())) {
            log.info("회사 ID [{}]는 이미 근태 규칙이 존재합니다.", company.getId());
            return;
        }

        AttendanceRule defaultRule = AttendanceRule.builder()
                .companyId(company.getId())
                .name("표준 근무 규칙")
                .defaultStartTime(LocalTime.of(9, 0))  // 출근 시간 09:00
                .defaultEndTime(LocalTime.of(18, 0))    // 퇴근 시간 18:00
                .defaultBreakMinutes(60)                // 휴게시간 60분
                .lateThresholdMin(10)                  // 10분 지각 허용
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        attendanceRuleRepository.save(defaultRule);
        log.info("회사 ID [{}]에 대한 기본 근태 규칙(09:00-18:00)이 생성되었습니다.", company.getId());
    }
}