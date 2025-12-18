package com.finalproj.orbitflow.attendance.attendanceRule.service;

import com.finalproj.orbitflow.attendance.attendanceRule.dto.AttRuleResDto;
import com.finalproj.orbitflow.attendance.attendanceRule.dto.AttRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.employeeAttRule.dto.EmpAttRuleCreateReqDto;
import com.finalproj.orbitflow.attendance.employeeAttRule.dto.EmpAttRuleResDto;
import com.finalproj.orbitflow.attendance.employeeAttRule.dto.EmpAttRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.attendanceRule.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.employeeAttRule.entity.EmployeeAttRule;
import com.finalproj.orbitflow.attendance.attendanceRule.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.attendance.employeeAttRule.repository.EmployeeAttRuleRepository;
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


    // 1. 회사 기본 규칙 (Default Rule) 관련

    /**
     * 1. 기본 규칙 조회
     */
    public AttRuleResDto getDefaultRule(Long companyId) {
        AttendanceRule rule = defaultRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회사의 기본 규칙을 찾을 수 없습니다."));

        return new AttRuleResDto(rule);
    }

    /**
     * 2. 기본 규칙 수정
     */
    @Transactional
    public AttRuleResDto updateDefaultRule(Long companyId, AttRuleUpdateReqDto request) {
        // 1. 해당 회사의 기본 규칙 조회
        AttendanceRule rule = defaultRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 기본 규칙이 존재하지 않습니다."));

        // 2. 값 업데이트 (Dirty Checking)
        rule.updateRule(
                request.defaultStartTime(),
                request.defaultEndTime(),
                request.defaultBreakMinutes()
        );

        return new AttRuleResDto(rule);
    }


    // 2. 사원별 예외 규칙 (Exception Rule) 관련

    /**
     * 3. 예외 규칙 목록 조회
     */
    public List<EmpAttRuleResDto> getExceptionRules(Long companyId) {
        // 해당 회사의 규칙만 조회 (Repository에 findByCompanyId 메서드 필요)
        List<EmployeeAttRule> rules = employeeRuleRepository.findByCompanyId(companyId);

        // N+1 방지를 위해 사원 ID 목록 추출 및 이름 매핑
        List<Long> employeeIds = rules.stream().map(EmployeeAttRule::getEmployeeId).toList();
        Map<Long, String> employeeMap = employeeRepository.findAllByIdIn(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        return rules.stream()
                .map(rule -> new EmpAttRuleResDto(rule, employeeMap.getOrDefault(rule.getEmployeeId(), "이름 미확인")))
                .toList();
    }

    /**
     * 4. 예외 규칙 상세 조회
     */
    public EmpAttRuleResDto getExceptionRuleDetail(Long companyId, Long ruleId) {
        EmployeeAttRule rule = employeeRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 규칙입니다."));

        // [보안] 조회하려는 규칙이 관리자의 회사 소속인지 최종 확인
        if (!rule.getCompanyId().equals(companyId)) {
            throw new SecurityException("해당 규칙에 접근할 권한이 없습니다.");
        }

        Employee employee = employeeRepository.findById(rule.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("사원 정보가 없습니다."));

        return new EmpAttRuleResDto(rule, employee.getName());
    }

    /**
     * 5. 예외 규칙 추가
     */
    @Transactional
    public EmpAttRuleResDto createExceptionRule(SecurityUser admin, EmpAttRuleCreateReqDto request) {
        // 1. 대상 사원 조회 및 존재 여부 확인
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new IllegalArgumentException("사원 정보를 찾을 수 없습니다. ID: " + request.employeeId()));

        // 2. [보안] 현재 로그인한 관리자의 회사와 사원의 회사가 일치하는지 검증
        if (!employee.getCompany().getId().equals(admin.getCompanyId())) {
            throw new SecurityException("해당 사원에 대한 규칙 설정 권한이 없습니다. (타사 사원)");
        }

        // 3. 엔티티 생성 및 저장 (Builder 사용)
        // 여기서 빨간줄이 뜬다면 EmployeeAttRule 엔티티에 @Builder가 있는지 확인하세요.
        EmployeeAttRule rule = EmployeeAttRule.builder()
                .companyId(admin.getCompanyId()) // 관리자의 세션 정보 사용
                .employeeId(request.employeeId())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .breakMinutes(request.breakMinutes())
                .reason(request.reason())
                .validFrom(request.validFrom())
                .validTo(request.validTo())
                .isActive(true) // 기본값 활성화
                .appliedAt(LocalDateTime.now())
//                .appliedBy(admin.getEmployeeId()) // 등록자 ID 저장
                .build();

        EmployeeAttRule savedRule = employeeRuleRepository.save(rule);

        // 4. 응답 DTO 반환 (사원 이름 포함)
        return new EmpAttRuleResDto(savedRule, employee.getName());
    }

    /**
     * 6. 예외 규칙 수정
     */
    @Transactional
    public EmpAttRuleResDto updateExceptionRule(SecurityUser admin, Long ruleId, EmpAttRuleUpdateReqDto request) {
        // 1. 수정할 규칙 존재 여부 확인
        EmployeeAttRule rule = employeeRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 규칙입니다. ID: " + ruleId));

        // 2. [보안] 관리자의 회사 ID와 규칙의 회사 ID가 일치하는지 검증
        if (!rule.getCompanyId().equals(admin.getCompanyId())) {
            throw new SecurityException("해당 규칙을 수정할 권한이 없습니다. (타사 데이터)");
        }

        // 3. 엔티티 업데이트 (Dirty Checking 활용)
        // DTO의 isActive가 null일 경우 기존 값을 유지하도록 처리
        rule.updateRule(
                request.startTime(),
                request.endTime(),
                request.breakMinutes(),
                request.reason(),
                request.validFrom(),
                request.validTo(),
                request.isActive() != null ? request.isActive() : rule.getIsActive()
        );

        // 4. 수정 시간 및 수정자(관리자) ID 기록
        rule.setAppliedAt(LocalDateTime.now());

        // rule.setAppliedBy(admin.getEmployeeId());

        // 5. 응답 시 사원 이름 포함 (규칙에 연결된 employeeId로 조회)
        Employee employee = employeeRepository.findById(rule.getEmployeeId()).orElse(null);
        String employeeName = (employee != null) ? employee.getName() : "알 수 없음";

        return new EmpAttRuleResDto(rule, employeeName);
    }

    /**
     * 7. 예외 규칙 삭제
     */
    @Transactional
    public void deleteExceptionRule(SecurityUser admin, Long ruleId) {
        // 1. 삭제 대상 존재 확인 및 권한 검증
        EmployeeAttRule rule = employeeRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 규칙 ID입니다: " + ruleId));

        if (!rule.getCompanyId().equals(admin.getCompanyId())) {
            throw new SecurityException("해당 규칙을 삭제할 권한이 없습니다.");
        }

        // 2. 삭제 실행
        employeeRuleRepository.delete(rule);
    }
}