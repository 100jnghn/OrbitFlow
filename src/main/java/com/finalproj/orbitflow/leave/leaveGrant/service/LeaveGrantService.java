package com.finalproj.orbitflow.leave.leaveGrant.service;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveHistoryResDto;
import com.finalproj.orbitflow.leave.leaveBalance.entity.LeaveBalance;
import com.finalproj.orbitflow.leave.leaveBalance.repository.LeaveBalanceRepository;
import com.finalproj.orbitflow.leave.leaveGrant.entity.LeaveGrant;
import com.finalproj.orbitflow.leave.leaveGrant.repository.LeaveGrantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveGrantService
 * @since : 2025. 12. 24. 수요일
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveGrantService {

    private final EmployeeRepository employeeRepository;
    private final LeaveGrantRepository leaveGrantRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    /**
     * [자동 실행 스케줄러]
     * cron = "0 0 0 1 * *" : 매달 1일 0시 0분 0초에 실행
     */
    @Scheduled(cron = "0 */10 * * * *")    // 테스트용
    @Transactional
    public void autoGrantLeave() {
        log.info("연차 자동 부여 스케줄러 실행: {}", LocalDate.now());
        grantAnnualLeave();
    }

    @Transactional
    public void grantAnnualLeave() {
        LocalDate today = LocalDate.now();
        List<Employee> activeEmployees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);

//        for (Employee emp : activeEmployees) {
//            // 1. 매년 1월 1일: 1년 이상 근무자 정기 연차 부여
//            if (today.getMonthValue() == 1 && today.getDayOfMonth() == 1) {
//                if (emp.getHireDate().isBefore(today.minusYears(1))) {
//                    processFullGrant(emp, today);
//                }
//            }
//
//            // 2. 매달 1일: 1년 미만 신입사원 월별 연차 부여
//            if (today.getDayOfMonth() == 1) {
//                if (emp.getHireDate().isAfter(today.minusYears(1))) {
//                    processMonthlyGrant(emp, today);
//                }
//            }
//        }

        for (Employee emp : activeEmployees) {
            // [테스트용] if (today.getDayOfMonth() == 1) 조건을 제거하여 매번 실행되게 함
            // 1. 1년 이상 근무자 (정기 연차)
            if (emp.getHireDate().isBefore(today.minusYears(1))) {
                processFullGrant(emp, today);
            }
            // 2. 1년 미만 근무자 (신입 월별 연차)
            else {
                processMonthlyGrant(emp, today);
            }
        }
    }

    private void processFullGrant(Employee emp, LocalDate today) {
        // 중복 부여 확인
        if (leaveGrantRepository.existsByEmployeeIdAndGrantTypeAndGrantDate(
                emp.getId(), "ANNUAL_REGULAR", today)) return;

        BigDecimal grantDays = new BigDecimal("15.00"); // 근속 연수에 따른 로직 확장 가능
        saveGrantAndBalance(emp, today, grantDays, "ANNUAL_REGULAR");
    }

    private void processMonthlyGrant(Employee emp, LocalDate today) {
        // 이번 달 이미 부여했는지 확인
        if (leaveGrantRepository.existsByEmployeeIdAndGrantTypeAndGrantDate(
                emp.getId(), "ANNUAL_MONTHLY", today)) return;

        BigDecimal grantDays = new BigDecimal("1.00");
        saveGrantAndBalance(emp, today, grantDays, "ANNUAL_MONTHLY");
    }


    private void saveGrantAndBalance(Employee emp, LocalDate today, BigDecimal days, String type) {
        // 1. LeaveGrant(이력) 생성 - Employee 및 Company 객체 활용
        LeaveGrant grant = LeaveGrant.builder()
                .employeeId(emp.getId())
                .companyId(emp.getCompany().getId()) // Employee 엔티티의 Company 참조 활용
                .grantDate(today)
                .grantedDays(days)
                .grantType(type)
                .expirationDate(today.plusYears(1).minusDays(1)) // 1년 유효 기간 설정
                .isExpired(false)
                .build();
        leaveGrantRepository.save(grant);

        //LeaveGrantService.java 내 수정
        LeaveBalance balance = leaveBalanceRepository.findByCompanyIdAndEmployeeIdAndYear(
                        emp.getCompany().getId(), // 1. companyId 추가
                        emp.getId(),              // 2. employeeId
                        today.getYear()           // 3. year
                )
                .orElseGet(() -> LeaveBalance.builder()
                        .companyId(emp.getCompany().getId())
                        .employeeId(emp.getId())
                        .year(today.getYear())
                        .totalGranted(BigDecimal.ZERO)
                        .remainingDays(BigDecimal.ZERO)
                        .build());

        balance.updateBalance(days); // 기존 잔합에 가산
        leaveBalanceRepository.save(balance);
    }


    // LeaveGrantService.java에 추가
    @Transactional(readOnly = true)
    public List<LeaveHistoryResDto> getHistoryByEmployee(Long companyId, Long employeeId) {
        // [보안 강화] 해당 회사 소속의 사원 데이터만 조회하도록 제한
        List<LeaveGrant> grants = leaveGrantRepository.findByCompanyIdAndEmployeeIdOrderByGrantDateDesc(companyId, employeeId);

        return grants.stream()
                .map(grant -> LeaveHistoryResDto.builder()
                        .title(getGrantTypeDisplay(grant.getGrantType()))
                        .actionDate(grant.getGrantDate().toString())
                        .period("-") // 부여 이력은 기간이 없으므로 "-" 처리
                        .days(grant.getGrantedDays())
                        .type("GRANT")
                        .statusName(grant.getIsExpired() ? "소멸" : "완료")
                        .statusCode(grant.getIsExpired() ? "EXPIRED" : "COMPLETED")
                        .build())
                .collect(Collectors.toList());
    }

    private String getGrantTypeDisplay(String type) {
        if ("ANNUAL_REGULAR".equals(type)) return "정기 연차 부여";
        if ("ANNUAL_MONTHLY".equals(type)) return "신입 월별 연차 부여";
        return type;
    }
}
