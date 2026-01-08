package com.finalproj.orbitflow.attendance.leave.service;

import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendanceRecord.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.attendance.leave.dto.*;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveBalance;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveGrant;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveBalanceRepository;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveGrantRepository;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveTypeRepository;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LeaveService {

    private final EmployeeRepository employeeRepository;
    private final LeaveGrantRepository leaveGrantRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    /**
     * [스케줄러/관리자] 정기 연차 부여 (회계년도 기준)
     */
    @Transactional
    public void batchGrantAnnualLeave(Long companyId, Integer year) {
        LocalDate grantDate = LocalDate.of(year, 1, 1);
        List<Employee> activeEmployees = employeeRepository.findByCompanyIdAndStatus(companyId, EmployeeStatus.ACTIVE);

        for (Employee emp : activeEmployees) {
            processEmployeeAnnualLeave(emp, grantDate, year);
        }
    }

    private void processEmployeeAnnualLeave(Employee emp, LocalDate grantDate, Integer year) {
        if (emp.getHireDate() == null) return;

        // 중복 부여 방지
        if (leaveGrantRepository.existsByEmployeeIdAndGrantTypeAndGrantDate(emp.getId(), "ANNUAL_REGULAR", grantDate)) {
            return;
        }

        BigDecimal grantDays;
        String type = "ANNUAL_REGULAR";

        // 입사 1년 이상 여부 확인
        if (!emp.getHireDate().isAfter(grantDate.minusYears(1))) {
            int yearsOfService = Period.between(emp.getHireDate(), grantDate).getYears();
            grantDays = calculateStandardDays(yearsOfService);
        } else {
            // 1년 미만자 비례 계산
            grantDays = calculateProportionalDays(emp.getHireDate(), year);
            type = "ANNUAL_PROPORTIONAL";
        }

        saveGrantAndBalance(emp, grantDate, grantDays, type);
    }


    /**
     * [스케줄러] 신입사원 월차 부여 (1년 미만자 매월 1일)
     */
    @Transactional
    public void grantMonthlyLeaveForCompany(Long companyId) {
        LocalDate today = LocalDate.now();
        List<Employee> active = employeeRepository.findByCompanyIdAndStatus(companyId, EmployeeStatus.ACTIVE);

        for (Employee e : active) {
            if (e.getHireDate() == null) continue;
            // 1년 이상자는 월차 부여 대상 제외
            if (!e.getHireDate().isAfter(today.minusYears(1))) continue;
            if (leaveGrantRepository.existsByEmployeeIdAndGrantTypeAndGrantDate(e.getId(), "ANNUAL_MONTHLY", today)) continue;

            saveGrantAndBalance(e, today, new BigDecimal("1.00"), "ANNUAL_MONTHLY");
        }
    }

    /**
     * [스케줄러] 연차 소멸 처리
     */
    @Transactional
    public void expireOutdatedLeaves() {
        LocalDate today = LocalDate.now();
        List<LeaveGrant> expired = leaveGrantRepository.findByExpirationDateBeforeAndIsExpiredFalse(today);

        for (LeaveGrant g : expired) {
            g.updateExpiredStatus(true);
            leaveBalanceRepository.findByCompanyIdAndEmployeeIdAndYear(g.getCompanyId(), g.getEmployeeId(), g.getGrantDate().getYear())
                    .ifPresent(b -> {
                        b.updateBalance(g.getGrantedDays().negate());
                        leaveBalanceRepository.save(b);
                    });
        }
    }


    /**
     * [API] 연차 현황 요약 데이터 생성
     */
    @Transactional
    public LeaveBalanceResDto getMySummary(Long companyId, Long employeeId, Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("사원을 찾을 수 없습니다."));

        LeaveBalance balance = leaveBalanceRepository.findByCompanyIdAndEmployeeIdAndYear(companyId, employeeId, targetYear)
                .orElseGet(() -> createInitialBalance(companyId, employeeId, targetYear));

        BigDecimal actualUsedDays = calculateActualUsedDays(companyId, employeeId, targetYear);
        balance.updateBalanceFromActualUsage(actualUsedDays);
        leaveBalanceRepository.save(balance);

        return LeaveBalanceResDto.builder()
                .year(targetYear)
                .totalGranted(balance.getTotalGranted())
                .usedDays(actualUsedDays)
                .remainingDays(balance.getRemainingDays())
                .hireDate(emp.getHireDate())
                .build();
    }


    /**
     * [API] 모든 휴가 신청 내역 통합 조회 (페이지네이션 해결)
     */
    public Page<LeaveHistoryResDto> getAllLeaveHistory(Long companyId, Long employeeId, LeaveSearchReqDto searchDto, Pageable pageable) {
        return attendanceRecordRepository.findAllLeaveHistoryWithFilters(
                        companyId, employeeId, searchDto.getTypeName(), searchDto.getStatus(),
                        searchDto.getStartDate(), searchDto.getEndDate(), pageable)
                .map(this::mapRecordToDto);
    }


    public Page<LeaveHistoryResDto> getLeaveUsageHistory(
            Long companyId, Long employeeId, int year,
            String typeName, DocumentStatus status, LocalDate startDate, LocalDate endDate,
            Pageable pageable) {
        // AttendanceRecordRepository에 작성된 쿼리를 사용하여 차감되는 항목만 조회
        // 필터: 승인됨(APPROVED) + 차감대상(isCountable=true) + 해당 연도 + 추가 필터
        return attendanceRecordRepository.findUsageHistoryWithFilters(
                        companyId, employeeId, year, typeName, status, startDate, endDate, pageable)
                .map(this::mapRecordToDto);
    }


    private BigDecimal calculateActualUsedDays(Long companyId, Long employeeId, int year) {
        return attendanceRecordRepository.findByCompanyIdAndEmployeeId(companyId, employeeId).stream()
                .filter(r -> r.getStatus() == DocumentStatus.APPROVED)
                .filter(r -> r.getLeaveType() != null && Boolean.TRUE.equals(r.getLeaveType().getIsCountable()))
                .filter(r -> r.getStartDate() != null && r.getStartDate().getYear() == year)
                .map(AttendanceRecord::getDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateProportionalDays(LocalDate hireDate, int targetYear) {
        long months = ChronoUnit.MONTHS.between(hireDate, LocalDate.of(targetYear, 1, 1));
        return new BigDecimal(months)
                .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("15"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateStandardDays(int yearsOfService) {
        int days = Math.min(15 + (yearsOfService - 1) / 2, 25);
        return new BigDecimal(days).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    protected void saveGrantAndBalance(Employee emp, LocalDate date, BigDecimal days, String type) {
        LeaveGrant grant = LeaveGrant.builder()
                .employeeId(emp.getId()).companyId(emp.getCompany().getId())
                .grantDate(date).grantedDays(days).grantType(type)
                .expirationDate(date.plusYears(1).minusDays(1)).isExpired(false).build();
        leaveGrantRepository.save(grant);

        LeaveBalance balance = leaveBalanceRepository.findByCompanyIdAndEmployeeIdAndYear(emp.getCompany().getId(), emp.getId(), date.getYear())
                .orElseGet(() -> createInitialBalance(emp.getCompany().getId(), emp.getId(), date.getYear()));

        balance.updateBalance(days);
        leaveBalanceRepository.save(balance);
    }

    private LeaveBalance createInitialBalance(Long companyId, Long employeeId, int year) {
        return LeaveBalance.builder()
                .companyId(companyId).employeeId(employeeId).year(year)
                .totalGranted(BigDecimal.ZERO).remainingDays(BigDecimal.ZERO).build();
    }

    private LeaveHistoryResDto mapRecordToDto(AttendanceRecord r) {
        String typeName = r.getLeaveType() != null ? r.getLeaveType().getTypeName() : "미지정";
        // 생성일 기준 액션 날짜 설정
        String actionDate = r.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDate().toString();

        return LeaveHistoryResDto.builder()
                .title(typeName)
                .actionDate(actionDate)
                .period(r.getStartDate() + " ~ " + (r.getEndDate() != null ? r.getEndDate() : r.getStartDate()))
                .days(r.getDays())
                .type("USED")
                .statusName(mapStatusText(r.getStatus()))
                .statusCode(r.getStatus().name())
                .reason(r.getReason())
                .typeDescription(r.getLeaveType() != null ? r.getLeaveType().getDescription() : "")
                .build();
    }

    private String mapStatusText(DocumentStatus status) {
        if (status == null) return "대기";
        return switch (status) {
            case APPROVED -> "승인";
            case REJECTED -> "반려";
            default -> "대기";
        };
    }


    public void deduction(Employee employee,
                          BigDecimal days,
                          Document document,
                          LeaveType leaveType) {

        if (!leaveType.getIsCountable()) return;

        if (document.getStatus() != DocumentStatus.APPROVED) return;

        LeaveBalance leaveBalance =
                leaveBalanceRepository
                        .findTopByEmployeeIdOrderByYearDesc(employee.getId())
                        .orElseThrow(() -> new NotFoundException("잔여 연차 조회 실패"));

        if (leaveBalance.getRemainingDays().compareTo(days) < 0) {
            throw new IllegalStateException("잔여 연차가 부족합니다.");
        }

        leaveBalance.deductBalance(days);
        leaveBalanceRepository.save(leaveBalance);
    }

    public LeaveRemainingResDto getLeaveRemaining(Long employeeId) {

        LeaveBalance leaveBalance =
                leaveBalanceRepository
                        .findTopByEmployeeIdOrderByYearDesc(employeeId)
                        .orElseThrow(() -> new NotFoundException("잔여 연차 조회 실패"));

        return new LeaveRemainingResDto(leaveBalance.getRemainingDays());
    }

    public LeaveValidationResDto validateLeave(
            Long employeeId,
            LeaveValidationReqDto reqDto
    ) {
        // 1. 잔여 연차 조회
        LeaveBalance leaveBalance =
                leaveBalanceRepository
                        .findTopByEmployeeIdOrderByYearDesc(employeeId)
                        .orElseThrow(() -> new NotFoundException("잔여 연차 조회 실패"));

        // 2. 휴가 유형 조회
        LeaveType leaveType =
                leaveTypeRepository.findById(reqDto.getLeaveTypeId())
                        .orElseThrow(() -> new NotFoundException("휴가 유형 조회 실패"));

        BigDecimal remainingDays = leaveBalance.getRemainingDays();

        /* =========================
           차감되지 않는 휴가
        ========================= */
        if (!leaveType.getIsCountable()) {
            return LeaveValidationResDto.builder()
                    .valid(true)
                    .requiredDays(BigDecimal.ZERO)
                    .remainingDays(remainingDays)
                    .message("연차 차감 대상이 아닌 휴가입니다")
                    .build();
        }

        /* =========================
           차감되는 휴가
        ========================= */

        // 3. 전체 기간 (inclusive)
        long totalDays =
                ChronoUnit.DAYS.between(
                        reqDto.getStartDate(),
                        reqDto.getEndDate()
                ) + 1;

        // 4. 필요 연차 계산
        BigDecimal requiredDays =
                leaveType.getUnitDays()
                        .multiply(BigDecimal.valueOf(totalDays));

        // 5. 검증
        boolean valid =
                remainingDays.compareTo(requiredDays) >= 0;

        String message = valid
                ? "신청 가능한 휴가입니다"
                : "잔여 연차가 부족합니다";

        return LeaveValidationResDto.builder()
                .valid(valid)
                .requiredDays(requiredDays)
                .remainingDays(remainingDays)
                .message(message)
                .build();
    }

}