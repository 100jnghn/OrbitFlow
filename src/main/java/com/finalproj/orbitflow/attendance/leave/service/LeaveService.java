package com.finalproj.orbitflow.attendance.leave.service;

import com.finalproj.orbitflow.approval.attendanceEvent.entity.AttendanceEvent;
import com.finalproj.orbitflow.approval.attendanceEvent.repository.AttendanceEventRepository;
import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.attendanceRecord.repository.AttendanceRecordRepository;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.CommuteRepository;
import com.finalproj.orbitflow.attendance.leave.dto.*;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveBalance;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveGrant;
import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveBalanceRepository;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveGrantRepository;
import com.finalproj.orbitflow.attendance.leave.repository.LeaveTypeRepository;
import com.finalproj.orbitflow.global.exception.BusinessException;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LeaveService {

    private final EmployeeRepository employeeRepository;
    private final LeaveGrantRepository leaveGrantRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final CommuteRepository commuteRepository;

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

        if (leaveGrantRepository.existsAnnualLeaveForYear(emp.getId(), year)) {
            log.info("사원 ID: {} - {}년도 연차가 이미 부여되어 건너뜁니다.", emp.getId(), year);
            return;
        }

        BigDecimal grantDays;
        String type;

        LocalDate standardDate = LocalDate.of(year, 1, 1);
        if (!emp.getHireDate().isAfter(standardDate.minusYears(1))) {
            int yearsOfService = Period.between(emp.getHireDate(), standardDate).getYears();
            grantDays = calculateStandardDays(yearsOfService);
            type = "ANNUAL_REGULAR";
        } else {
            grantDays = calculateProportionalDays(emp.getHireDate(), year);
            type = "ANNUAL_PROPORTIONAL";
        }

        saveGrantAndBalance(emp, grantDate, grantDays, type);
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
        // 필터: 차감대상(isCountable=true) + 해당 연도 + 추가 필터 (상태 조건 제거됨)
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


    @Transactional
    public void updateWorkStatus(Long employeeId, WorkStatus status) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("사원을 찾을 수 없습니다."));
        employee.updateWorkStatus(status);
    }



    @Transactional
    public void updateAllEmployeesWorkStatus(LocalDate today) {

        List<WorkStatus> specialStatuses = getSpecialWorkStatuses();

        // 기존 특수 상태 초기화
        List<Employee> specialStatusEmployees = employeeRepository.findByStatusAndWorkStatusIn(
                EmployeeStatus.ACTIVE, specialStatuses);

        for (Employee emp : specialStatusEmployees) {
            emp.updateWorkStatus(WorkStatus.OFF_WORK);
        }

        // 오늘 기준 유효한 출근/휴가 기록 조회
        List<AttendanceRecord> activeRecords = attendanceRecordRepository.findActiveAttendanceRecords(today);

        for (AttendanceRecord record : activeRecords) {
            Employee employee = record.getEmployee();
            if (record.getSourceDocument() == null || record.getSourceDocument().getTemplateGroup() == null)
                continue;

            // 승인 문서(BaseRole)를 기준으로 최종 상태 결정
            BaseRole role = record.getSourceDocument().getTemplateGroup().getBaseRole();

            try {
                WorkStatus targetStatus = WorkStatus.valueOf(role.name());
                employee.updateWorkStatus(targetStatus);
            } catch (IllegalArgumentException e) {
            }
        }
    }


    private List<WorkStatus> getSpecialWorkStatuses() {
        return List.of(
                WorkStatus.VACATION,
                WorkStatus.BUSINESS_TRIP,
                WorkStatus.OUTWORK
        );
    }





    /**
     * 신규 사원 가입(입사) 직후 비례 연차 즉시 부여
     * EmployeeService 또는 AdminController에서 사원 객체를 전달받아 호출합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
                yearEnd.plusMonths(1).withDayOfMonth(1));

        if (remainingMonths <= 0)
            return BigDecimal.ZERO;

        return new BigDecimal(remainingMonths)
                .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("15"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("ID [" + employeeId + "] 사원을 찾을 수 없습니다."));
    }


    @Transactional
    public void normalizeTodayAttendanceStatus(Long companyId, LocalDate today) {

        List<Employee> employees =
                employeeRepository.findByCompanyIdAndStatus(companyId, EmployeeStatus.ACTIVE);

        for (Employee employee : employees) {

            WorkStatus workStatus = employee.getWorkStatus();
            if (workStatus == null) continue;

            AttendanceStatus targetStatus = mapToAttendanceStatus(workStatus);
            if (targetStatus == null) continue;

            upsertTodayAttendance(
                    companyId,
                    employee.getId(),
                    today,
                    targetStatus
            );
        }
    }


    /**
     * ✅ WorkStatus → AttendanceStatus 매핑
     */
    private AttendanceStatus mapToAttendanceStatus(WorkStatus workStatus) {
        return switch (workStatus) {
            case VACATION -> AttendanceStatus.VACATION;
            case OUTWORK -> AttendanceStatus.OUTSIDE;
            case BUSINESS_TRIP -> AttendanceStatus.BUSINESS_TRIP;
            default -> null;
        };
    }

    /**
     * ✅ 오늘자 attendance upsert
     * - 없으면 생성
     * - 있으면 상태만 자동 보정 (정정 아님)
     */
    @Transactional
    protected void upsertTodayAttendance(
            Long companyId,
            Long employeeId,
            LocalDate workDate,
            AttendanceStatus targetStatus
    ) {

        Attendance attendance = commuteRepository
                .findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, workDate)
                .orElse(null);

        if (attendance == null) {
            Attendance created = Attendance.builder()
                    .companyId(companyId)
                    .employeeId(employeeId)
                    .workDate(workDate)
                    .commuteAt(null)
                    .leaveAt(null)
                    .isCorrected(false)
                    .status(targetStatus)
                    .build();

            commuteRepository.save(created);
            return;
        }

        attendance.updateStatusAutomatically(targetStatus);
        commuteRepository.save(attendance);
    }





}