package com.finalproj.orbitflow.leave.leaveGrant.service;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.leave.leaveBalance.entity.LeaveBalance;
import com.finalproj.orbitflow.leave.leaveBalance.repository.LeaveBalanceRepository;
import com.finalproj.orbitflow.leave.leaveGrant.entity.LeaveGrant;
import com.finalproj.orbitflow.leave.leaveGrant.repository.LeaveGrantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveGrantService {

    private final EmployeeRepository employeeRepository;
    private final LeaveGrantRepository leaveGrantRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    /**
     * [스케줄러] 매일 00:00:05 실행
     * 소멸 처리 및 신입사원 월차 부여 수행
     */
    @Scheduled(cron = "5 0 0 * * *")
    @Transactional
    public void processDailyLeaveJob() {
        log.info("일일 연차 관리 작업 시작: {}", LocalDate.now());
        expireOutdatedLeaves();    // 1. 소멸 처리
        grantMonthlyLeaveForNewbies(); // 2. 신입 월차 부여
    }

    /**
     * 회계년도(1월 1일) 기준 연차 일괄 부여 (관리자 또는 스케줄러 호출)
     */
    @Transactional
    public void batchGrantAnnualLeave(Long companyId, Integer year) {
        LocalDate grantDate = LocalDate.of(year, 1, 1);
        List<Employee> activeEmployees = employeeRepository.findByCompanyIdAndStatus(companyId, EmployeeStatus.ACTIVE);

        for (Employee emp : activeEmployees) {
            // 1년 미만 근무자 제외 (신입 월차 로직에서 관리)
            if (emp.getHireDate().isAfter(grantDate.minusYears(1))) continue;

            // 중복 부여 확인
            if (leaveGrantRepository.existsByEmployeeIdAndGrantTypeAndGrantDate(emp.getId(), "ANNUAL_REGULAR", grantDate)) continue;

            // 근속연수 계산 및 연차 개수 산출 (2년마다 1일 가산, 최대 25일)
            int yearsOfService = Period.between(emp.getHireDate(), grantDate).getYears();
            BigDecimal grantDays = calculateAnnualLeaveDays(yearsOfService);

            saveGrantAndBalance(emp, grantDate, grantDays, "ANNUAL_REGULAR");
        }
    }

    /**
     * 연차 자동 소멸 로직
     */
    @Transactional
    public void expireOutdatedLeaves() {
        LocalDate today = LocalDate.now();
        List<LeaveGrant> expiredGrants = leaveGrantRepository.findByExpirationDateBeforeAndIsExpiredFalse(today);

        for (LeaveGrant grant : expiredGrants) {
            grant.updateExpiredStatus(true); // Entity 내 구현 필요

            // 해당 연도의 Balance에서 소멸된 만큼 차감
            leaveBalanceRepository.findByCompanyIdAndEmployeeIdAndYear(
                            grant.getCompanyId(), grant.getEmployeeId(), grant.getGrantDate().getYear())
                    .ifPresent(balance -> balance.updateBalance(grant.getGrantedDays().negate()));

            log.info("사원 ID: {} - {}년도 연차 {}일 소멸 완료", grant.getEmployeeId(), grant.getGrantDate().getYear(), grant.getGrantedDays());
        }
    }

    /**
     * 1년 미만 신입사원 월별 연차 부여
     */
    @Transactional
    public void grantMonthlyLeaveForNewbies() {
        LocalDate today = LocalDate.now();
        List<Employee> activeEmployees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);

        for (Employee emp : activeEmployees) {
            // 입사 1년 미만인 경우만 대상
            if (!emp.getHireDate().isAfter(today.minusYears(1))) continue;

            if (leaveGrantRepository.existsByEmployeeIdAndGrantTypeAndGrantDate(emp.getId(), "ANNUAL_MONTHLY", today)) continue;

            saveGrantAndBalance(emp, today, new BigDecimal("1.00"), "ANNUAL_MONTHLY");
        }
    }

    private BigDecimal calculateAnnualLeaveDays(int yearsOfService) {
        int totalDays = Math.min(15 + (yearsOfService - 1) / 2, 25);
        return new BigDecimal(totalDays).setScale(2);
    }

    private void saveGrantAndBalance(Employee emp, LocalDate today, BigDecimal days, String type) {
        // 1. LeaveGrant(이력) 생성
        LeaveGrant grant = LeaveGrant.builder()
                .employeeId(emp.getId())
                .companyId(emp.getCompany().getId())
                .grantDate(today)
                .grantedDays(days)
                .grantType(type)
                .expirationDate(today.plusYears(1).minusDays(1)) // 1년 유효
                .isExpired(false)
                .build();
        leaveGrantRepository.save(grant);

        // 2. LeaveBalance(잔액) 업데이트
        LeaveBalance balance = leaveBalanceRepository.findByCompanyIdAndEmployeeIdAndYear(
                        emp.getCompany().getId(), emp.getId(), today.getYear())
                .orElseGet(() -> LeaveBalance.builder()
                        .companyId(emp.getCompany().getId()).employeeId(emp.getId()).year(today.getYear())
                        .totalGranted(BigDecimal.ZERO).remainingDays(BigDecimal.ZERO).build());

        balance.updateBalance(days);
        leaveBalanceRepository.save(balance);
    }
}