package com.finalproj.orbitflow.leave.leaveGrant.service;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import com.finalproj.orbitflow.leave.leaveBalance.entity.LeaveBalance;
import com.finalproj.orbitflow.leave.leaveBalance.repository.LeaveBalanceRepository;
import com.finalproj.orbitflow.leave.leaveGrant.entity.LeaveGrant;
import com.finalproj.orbitflow.leave.leaveGrant.repository.LeaveGrantRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveGrantService
 * @since : 2025. 12. 24. 수요일
 */
@Service
@RequiredArgsConstructor
public class LeaveGrantService {

    private final EmployeeRepository employeeRepository;
    private final LeaveGrantRepository leaveGrantRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Transactional
    public void grantAnnualLeave() {
        LocalDate today = LocalDate.now();
        List<Employee> activeEmployees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);

        for (Employee emp : activeEmployees) {
            // 1. 매년 1월 1일: 1년 이상 근무자 정기 연차 부여
            if (today.getMonthValue() == 1 && today.getDayOfMonth() == 1) {
                if (emp.getHireDate().isBefore(today.minusYears(1))) {
                    processFullGrant(emp, today);
                }
            }

            // 2. 매달 1일: 1년 미만 신입사원 월별 연차 부여
            if (today.getDayOfMonth() == 1) {
                if (emp.getHireDate().isAfter(today.minusYears(1))) {
                    processMonthlyGrant(emp, today);
                }
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
}
