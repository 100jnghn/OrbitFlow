package com.finalproj.orbitflow.leave.leaveGrant.service;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.leave.leaveBalance.entity.LeaveBalance;
import com.finalproj.orbitflow.leave.leaveBalance.repository.LeaveBalanceRepository;
import com.finalproj.orbitflow.leave.leaveGrant.entity.LeaveGrant;
import com.finalproj.orbitflow.leave.leaveGrant.repository.LeaveGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private final LeaveGrantRepository grantRepository;
    private final LeaveBalanceRepository balanceRepository;
    private final EmployeeRepository employeeRepository; // 전 직원 조회를 위해 추가 필요

    /**
     * [스케줄러] 매년 1월 1일 0시 0분에 전사적 연차 부여 실행
     */
    @Scheduled(cron = "0 0 0 1 1 ?")
    @Transactional
    public void processAnnualLeaveGrant() {
        int currentYear = LocalDate.now().getYear();
        // 모든 활성 직원 조회 (퇴사자 제외 등 조건 필요)
        List<Employee> allEmployees = employeeRepository.findAllByStatus("ACTIVE");

        for (Employee emp : allEmployees) {
            grantAnnualLeave(emp, currentYear);
        }
    }


    @Transactional
    public void grantAnnualLeave(Employee emp, int year) {
        Long companyId = emp.getCompanyId();
        Long empId = emp.getId();
        LocalDate hireDate = emp.getHireDate();
        LocalDate baseDate = LocalDate.of(year, 1, 1);

        // 1. 근속 연수 계산
        long serviceYears = ChronoUnit.YEARS.between(hireDate, baseDate);
        BigDecimal grantDays;

        if (serviceYears < 1) {
            // 1년 미만 신입: (입사일부터 연말까지 근속월수 / 12) * 15일
            long months = ChronoUnit.MONTHS.between(hireDate, baseDate);
            grantDays = BigDecimal.valueOf(15)
                    .multiply(BigDecimal.valueOf(months))
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        } else {
            // 1년 이상: 기본 15일 + 가산 연차 (2년마다 1일, 최대 25일)
            int extraDays = (int) (serviceYears - 1) / 2;
            int totalDays = Math.min(15 + extraDays, 25);
            grantDays = BigDecimal.valueOf(totalDays);
        }

        // 2. LeaveGrant (부여 이력) 저장
        LeaveGrant grant = LeaveGrant.builder()
                .companyId(companyId)
                .employeeId(empId)
                .grantedDays(grantDays)
                .grantDate(baseDate)
                .grantType("정기부여")
                .year(year)
                .build();
        grantRepository.save(grant);

        // 3. LeaveBalance (잔액 요약) 업데이트
        LeaveBalance balance = balanceRepository.findByCompanyIdAndEmployeeIdAndYear(companyId, empId, year)
                .orElse(LeaveBalance.builder()
                        .companyId(companyId)
                        .employeeId(empId)
                        .year(year)
                        .totalGranted(BigDecimal.ZERO)
                        .totalUsed(BigDecimal.ZERO)
                        .remainingDays(BigDecimal.ZERO)
                        .build());

        balance.updateByGrant(grantDays); // totalGranted와 remainingDays 증가
        balanceRepository.save(balance);
    }
}
