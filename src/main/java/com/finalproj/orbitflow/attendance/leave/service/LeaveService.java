package com.finalproj.orbitflow.attendance.leave.service;

import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendanceRecord.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
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
import com.finalproj.orbitflow.hr.employee.enums.WorkStatus;
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
    private final com.finalproj.orbitflow.approval.attendanceEvent.repository.AttendanceEventRepository attendanceEventRepository;
    private final com.finalproj.orbitflow.schedule.repository.ScheduleRepository scheduleRepository;

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
        if (emp.getHireDate() == null)
            return;

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
            if (e.getHireDate() == null)
                continue;

            // 1년 미만 여부 판별
            if (!e.getHireDate().isAfter(today.minusYears(1)))
                continue;
            if (leaveGrantRepository.existsByEmployeeIdAndGrantTypeAndGrantDate(e.getId(), "ANNUAL_MONTHLY", today))
                continue;

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
            leaveBalanceRepository
                    .findByCompanyIdAndEmployeeIdAndYear(g.getCompanyId(), g.getEmployeeId(),
                            g.getGrantDate().getYear())
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

        LeaveBalance balance = leaveBalanceRepository
                .findByCompanyIdAndEmployeeIdAndYear(companyId, employeeId, targetYear)
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
     * [API] 모든 휴가 신청 내역 통합 조회
     */
    public Page<LeaveHistoryResDto> getAllLeaveHistory(Long companyId, Long employeeId, LeaveSearchReqDto searchDto,
            Pageable pageable) {
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

        LeaveBalance balance = leaveBalanceRepository
                .findByCompanyIdAndEmployeeIdAndYear(emp.getCompany().getId(), emp.getId(), date.getYear())
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
        if (status == null)
            return "대기";
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

        if (!leaveType.getIsCountable())
            return;

        if (document.getStatus() != DocumentStatus.APPROVED)
            return;

        LeaveBalance leaveBalance = leaveBalanceRepository
                .findTopByEmployeeIdOrderByYearDesc(employee.getId())
                .orElseThrow(() -> new NotFoundException("잔여 연차 조회 실패"));

        if (leaveBalance.getRemainingDays().compareTo(days) < 0) {
            throw new IllegalStateException("잔여 연차가 부족합니다.");
        }

        leaveBalance.deductBalance(days);
        leaveBalanceRepository.save(leaveBalance);
    }

    public LeaveRemainingResDto getLeaveRemaining(Long employeeId) {

        LeaveBalance leaveBalance = leaveBalanceRepository
                .findTopByEmployeeIdOrderByYearDesc(employeeId)
                .orElseThrow(() -> new NotFoundException("잔여 연차 조회 실패"));

        return new LeaveRemainingResDto(leaveBalance.getRemainingDays());
    }

    public LeaveValidationResDto validateLeave(
            Long employeeId,
            LeaveValidationReqDto reqDto) {
        // 1. 잔여 연차 조회
        LeaveBalance leaveBalance = leaveBalanceRepository
                .findTopByEmployeeIdOrderByYearDesc(employeeId)
                .orElseThrow(() -> new NotFoundException("잔여 연차 조회 실패"));

        // 2. 휴가 유형 조회
        LeaveType leaveType = leaveTypeRepository.findById(reqDto.getLeaveTypeId())
                .orElseThrow(() -> new NotFoundException("휴가 유형 조회 실패"));

        BigDecimal remainingDays = leaveBalance.getRemainingDays();

        /*
         * =========================
         * 차감되지 않는 휴가
         * =========================
         */
        if (!leaveType.getIsCountable()) {
            return LeaveValidationResDto.builder()
                    .valid(true)
                    .requiredDays(BigDecimal.ZERO)
                    .remainingDays(remainingDays)
                    .message("연차 차감 대상이 아닌 휴가입니다")
                    .build();
        }

        /*
         * =========================
         * 차감되는 휴가
         * =========================
         */

        // 3. 전체 기간 (inclusive)
        long totalDays = ChronoUnit.DAYS.between(
                reqDto.getStartDate(),
                reqDto.getEndDate()) + 1;

        // 4. 필요 연차 계산
        BigDecimal requiredDays = leaveType.getUnitDays()
                .multiply(BigDecimal.valueOf(totalDays));

        // 5. 검증
        boolean valid = remainingDays.compareTo(requiredDays) >= 0;

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

    @Transactional
    public void updateWorkStatus(Long employeeId, WorkStatus status) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("사원을 찾을 수 없습니다."));
        employee.updateWorkStatus(status);
    }

    /**
     * 🚀 [수정] 통일된 Enum 값에 맞춰 매핑 로직 단순화
     */
    @Transactional
    public void updateAllEmployeesWorkStatus(LocalDate today) {
        log.info("[Scheduler] 전사 근무 상태 동기화 시작: {}", today);

        // 1. 복구 로직: 특수 상태 사원 초기화
        List<WorkStatus> specialStatuses = List.of(
                WorkStatus.VACATION,
                WorkStatus.BUSINESS_TRIP,
                WorkStatus.OUTWORK);

        List<Employee> specialStatusEmployees = employeeRepository.findByStatusAndWorkStatusIn(
                EmployeeStatus.ACTIVE, specialStatuses);

        for (Employee emp : specialStatusEmployees) {
            emp.updateWorkStatus(WorkStatus.OFF_WORK);
        }

        // 2. 오늘 날짜 승인 근태 기록 적용
        List<AttendanceRecord> activeRecords = attendanceRecordRepository.findActiveAttendanceRecords(today);

        for (AttendanceRecord record : activeRecords) {
            Employee employee = record.getEmployee();
            if (record.getSourceDocument() == null || record.getSourceDocument().getTemplateGroup() == null) continue;

            BaseRole role = record.getSourceDocument().getTemplateGroup().getBaseRole();

            // 🚀 [핵심] BaseRole과 WorkStatus가 동일하므로 직접 변환 가능
            try {
                WorkStatus targetStatus = WorkStatus.valueOf(role.name());
                employee.updateWorkStatus(targetStatus);
                log.info("[Scheduler] 사원: {}, 상태 변경: {}", employee.getName(), targetStatus);
            } catch (IllegalArgumentException e) {
                log.warn("매핑되지 않는 BaseRole: {}", role);
            }
        }
    }

    /**
     * [API] 조기 복귀 처리
     * - 현재 진행 중인 휴가/출장 기록을 오늘 날짜 기준으로 종료 처리
     * - 잔여 연차 환불 (연차 차감 대상인 경우)
     * - 미래의 근태 이벤트 및 일정 삭제
     * - 사원 상태 '근무중'으로 변경
     */
    @Transactional
    public void processEarlyReturn(Long employeeId) {
        LocalDate today = LocalDate.now();

        // 1. 현재 진행 중인 근태 기록 조회
        AttendanceRecord record = attendanceRecordRepository.findActiveRecord(employeeId, today)
                .orElseThrow(() -> new NotFoundException("현재 진행 중인 휴가/출장 기록이 없습니다."));

        // 2. 사원 조회
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("사원을 찾을 수 없습니다."));

        // 3. 기록 단축 및 환불 계산
        LocalDate originalEndDate = record.getEndDate();

        // 이미 종료된 기록이면 패스 (쿼리에서 걸러지지만 안전장치)
        if (originalEndDate.isBefore(today)) {
            throw new IllegalStateException("이미 종료된 휴가입니다.");
        }

        // 새 종료일: 어제 (오늘 복귀했으므로 어제까지만 휴가)
        LocalDate newEndDate = today.minusDays(1);

        // 전체 취소 여부 (시작일이 오늘이거나 미래인 경우) -> 로직상 start <= today 이므로 start == today 인 경우
        boolean isFullCancellation = record.getStartDate().isAfter(newEndDate);

        // 사용일수 재계산
        BigDecimal newUsedDays;
        if (isFullCancellation) {
            newUsedDays = BigDecimal.ZERO;
        } else {
            // 🔥 null 체크 강화: 출장/외근의 경우 leaveType이 null일 수 있음
            if (record.getLeaveType() == null) {
                // 출장/외근의 경우 연차 차감이 없으므로 0
                newUsedDays = BigDecimal.ZERO;
            } else {
                long dayCount = ChronoUnit.DAYS.between(record.getStartDate(), newEndDate) + 1;
                newUsedDays = record.getLeaveType().getUnitDays().multiply(BigDecimal.valueOf(dayCount));
            }
        }

        // 환불할 일수 = 기존 차감 일수 - 실제 사용 일수
        BigDecimal refundDays = record.getDays().subtract(newUsedDays);

        // 4. 연차 환불 처리 (차감 대상인 경우)
        // 🔥 null 체크 강화: 출장/외근의 경우 leaveType이 null일 수 있음
        if (record.getLeaveType() != null 
                && Boolean.TRUE.equals(record.getLeaveType().getIsCountable()) 
                && refundDays.compareTo(BigDecimal.ZERO) > 0) {
            LeaveBalance balance = leaveBalanceRepository
                    .findTopByEmployeeIdOrderByYearDesc(employeeId)
                    .orElseThrow(() -> new NotFoundException("잔여 연차 정보를 찾을 수 없습니다."));

            balance.updateBalance(refundDays);
        }

        // 5. 기록 업데이트
        record.updateDuration(newEndDate, newUsedDays);

        // 6. 미래 이벤트 및 일정 삭제
        attendanceEventRepository.deleteByEmployeeIdAndStartDateAfter(employeeId, today);

        // 오늘 이후(내일 0시부터)의 일정 삭제
        scheduleRepository.deleteByEmployeeIdAndStartAtAfter(employeeId, today.atTime(23, 59, 59));

        // 7. 상태 변경
        employee.updateWorkStatus(WorkStatus.WORKING);

        log.info("조기 복귀 처리 완료 - 사원: {}, 환불일수: {}", employee.getName(), refundDays);
    }


    // -----------------


    /**
     * 신규 사원 가입(입사) 직후 비례 연차 즉시 부여
     * EmployeeService 또는 AdminController에서 사원 객체를 전달받아 호출합니다.
     */
    @Transactional
    public void grantInitialLeave(Employee emp) {
        // 1. 입사일 존재 여부 확인
        if (emp == null || emp.getHireDate() == null) {
            log.warn("[InitialGrant] 사원 정보 또는 입사일이 누락되어 연차 부여를 건너뜁니다.");
            return;
        }

        LocalDate hireDate = emp.getHireDate();
        // 2. 입사일 기준 당해 연도 비례 연차 계산 (입사월~12월)
        // 공식: (남은 근무 월수 / 12) * 15
        BigDecimal grantDays = calculateImmediateProportionalDays(hireDate);
        String type = "ANNUAL_PROPORTIONAL";

        // 3. 중복 부여 방지
        if (leaveGrantRepository.existsByEmployeeIdAndGrantTypeAndGrantDate(emp.getId(), type, hireDate)) {
            log.info("[InitialGrant] 이미 부여된 이력이 있습니다. 사원: {}", emp.getName());
            return;
        }

        // 4. 부여 내역 저장 및 Balance 업데이트
        saveGrantAndBalance(emp, hireDate, grantDays, type);
        log.info("[InitialGrant] 사원: {}, 입사일: {}, 부여연차: {}일", emp.getName(), hireDate, grantDays);
    }

    /**
     * 🚀 [신규] 입사 시점 기준 당해 연도 잔여 개월 비례 계산 로직
     * 공식: (근무 가능 월수 / 12) * 15
     */
    private BigDecimal calculateImmediateProportionalDays(LocalDate hireDate) {
        // 입사월부터 12월까지의 개월 수 계산
        LocalDate yearEnd = LocalDate.of(hireDate.getYear(), 12, 31);

        // 시작월부터 종료월까지 포함하기 위해 날짜 조정 후 계산
        long remainingMonths = ChronoUnit.MONTHS.between(
                hireDate.withDayOfMonth(1),
                yearEnd.plusMonths(1).withDayOfMonth(1)
        );

        if (remainingMonths <= 0) return BigDecimal.ZERO;

        return new BigDecimal(remainingMonths)
                .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("15"))
                .setScale(2, RoundingMode.HALF_UP);
    }


    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("ID [" + employeeId + "] 사원을 찾을 수 없습니다."));
    }
}