package com.finalproj.orbitflow.attendance.dashboard.service;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.CommuteRepository;
import com.finalproj.orbitflow.attendance.dashboard.dto.AdminAttendanceResDto;
import com.finalproj.orbitflow.attendance.dashboard.dto.AdminSummaryResDto;
import com.finalproj.orbitflow.attendance.dashboard.dto.AttendanceUpdateDto;
import com.finalproj.orbitflow.attendance.rule.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.rule.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.enums.WorkStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // ✅ 정확한 임포트 확인
import com.finalproj.orbitflow.notification.service.NotificationCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceDashboardService {

    private final CommuteRepository commuteRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRuleRepository attendanceRuleRepository;
    private final NotificationCommandService notificationCommandService;

    // 요약 통계
    @Transactional(readOnly = true)
    public AdminSummaryResDto getTodaySummary(Long companyId) {
        LocalDate today = LocalDate.now();

        // 1. 해당 회사의 모든 재직 중인 사원 목록을 한 번에 가져옵니다.
        List<Employee> activeEmployees = employeeRepository.findByCompanyIdAndStatus(companyId, EmployeeStatus.ACTIVE);
        int totalActive = activeEmployees.size();

        // 2. Employee 테이블의 work_status 컬럼을 기준으로 그룹핑하여 카운트합니다. (실시간 상태)
        Map<WorkStatus, Long> statusCounts = activeEmployees.stream()
                .collect(Collectors.groupingBy(Employee::getWorkStatus, Collectors.counting()));

        // 3. 실시간 상태(WorkStatus)에서 휴가/외근/출장 인원 추출
        int vacation = statusCounts.getOrDefault(WorkStatus.VACATION, 0L).intValue();
        int outside = statusCounts.getOrDefault(WorkStatus.OUTWORK, 0L).intValue();
        int businessTrip = statusCounts.getOrDefault(WorkStatus.BUSINESS_TRIP, 0L).intValue();

        // 4. 근태 기록(Attendance) 테이블에서 출근 및 지각 인원 추출
        // 출근 버튼을 누른 결과(ON_TIME, LATE)는 기록 테이블에서 가져오는 것이 정확합니다.
        int onTimeRecord = commuteRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today,
                AttendanceStatus.ON_TIME);
        int lateRecord = commuteRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today,
                AttendanceStatus.LATE);

        // 5. 결근/미출근 인원 계산
        // 전체 인원 - (출근 기록이 있는 인원 + 현재 휴가/외근/출장 중인 인원)
        int totalPresent = onTimeRecord + lateRecord + vacation + outside + businessTrip;
        int absentCount = Math.max(0, totalActive - totalPresent);

        return AdminSummaryResDto.builder()
                .totalEmployees(totalActive)
                .onTimeCount(onTimeRecord + lateRecord)
                .lateCount(lateRecord)
                .vacationCount(vacation)
                .outsideCount(outside)
                .businessTripCount(businessTrip)
                .absentCount(absentCount)
                .build();
    }

    public Page<AdminAttendanceResDto> getCompanyAttendanceList(
            Long companyId, String start, String end, String status, String keyword, Pageable pageable) {

        // 1. 기간 설정: 시작일만 있으면 당일, 시작/종료일 모두 있으면 해당 범위
        LocalDate startDate = (start == null || start.isEmpty()) ? LocalDate.now() : LocalDate.parse(start);
        LocalDate endDate = (end == null || end.isEmpty()) ? startDate : LocalDate.parse(end);

        AttendanceStatus statusEnum = (status == null || status.equals("ALL")) ? null
                : AttendanceStatus.valueOf(status);

        // 2. 기본 근태 규칙 조회 (출근/퇴근 시간 확인용)
        AttendanceRule defaultRule = attendanceRuleRepository
                .findByCompanyIdAndIsDefaultTrue(companyId)
                .orElse(null);

        // 3. WorkStatus 매핑
        WorkStatus targetWorkStatus = null;
        if (statusEnum != null) {
            switch (statusEnum) {
                case VACATION -> targetWorkStatus = WorkStatus.VACATION;
                case BUSINESS_TRIP -> targetWorkStatus = WorkStatus.BUSINESS_TRIP;
                case OUTSIDE -> targetWorkStatus = WorkStatus.OUTWORK;
            }
        }

        // 4. 수정된 리포지토리 메서드 호출
        return commuteRepository.findAllEmployeesWithAttendance(
                companyId, startDate, endDate, statusEnum, targetWorkStatus, keyword, pageable)
                .map(result -> {
                    Employee emp = (Employee) result[0];
                    Attendance att = (Attendance) result[1];
                    // 기록이 있으면 기록 날짜, 없으면 조회 시작일로 표시
                    LocalDate recordDate = (att != null) ? att.getWorkDate() : startDate;
                    return convertToCombinedDto(emp, att, recordDate, defaultRule);
                });
    }

    /**
     * 엔티티 결합 및 DTO 변환 (근무 시간 계산 로직 추가)
     */
    private AdminAttendanceResDto convertToCombinedDto(Employee emp, Attendance att, LocalDate date,
            AttendanceRule defaultRule) {
        // 출근 기록이 없는 경우: 시간에 따라 "근무예정" 또는 "결근" 판단
        String statusName;
        String statusCode;
        if (att != null) {
            statusName = att.getStatus().getDescription();
            statusCode = att.getStatus().name();
        } else {
            // 출근 기록이 없는 경우
            if (date.equals(LocalDate.now())) {
                // [수정] 오늘 날짜인 경우, Employee의 현재 상태(WorkStatus)를 우선 확인
                if (emp.getWorkStatus() == WorkStatus.VACATION) {
                    statusName = AttendanceStatus.VACATION.getDescription();
                    statusCode = AttendanceStatus.VACATION.name();
                } else if (emp.getWorkStatus() == WorkStatus.BUSINESS_TRIP) {
                    statusName = AttendanceStatus.BUSINESS_TRIP.getDescription();
                    statusCode = AttendanceStatus.BUSINESS_TRIP.name();
                } else if (emp.getWorkStatus() == WorkStatus.OUTWORK) {
                    statusName = AttendanceStatus.OUTSIDE.getDescription();
                    statusCode = AttendanceStatus.OUTSIDE.name();
                } else if (defaultRule != null) {
                    // 기본 규칙이 있는 경우 시간 비교
                    LocalTime now = LocalTime.now();
                    LocalTime startTime = defaultRule.getDefaultStartTime();
                    LocalTime endTime = defaultRule.getDefaultEndTime();

                    if (now.isBefore(startTime)) {
                        statusName = AttendanceStatus.BEFORE_WORK.getDescription();
                        statusCode = AttendanceStatus.BEFORE_WORK.name();
                    } else if (now.isAfter(endTime)) {
                        statusName = AttendanceStatus.ABSENT.getDescription();
                        statusCode = AttendanceStatus.ABSENT.name();
                    } else {
                        statusName = AttendanceStatus.BEFORE_WORK.getDescription();
                        statusCode = AttendanceStatus.BEFORE_WORK.name();
                    }
                } else {
                    statusName = AttendanceStatus.BEFORE_WORK.getDescription();
                    statusCode = AttendanceStatus.BEFORE_WORK.name();
                }
            } else {
                // 과거 날짜면 "결근"
                statusName = AttendanceStatus.ABSENT.getDescription();
                statusCode = AttendanceStatus.ABSENT.name();
            }
        }

        // 근무 시간 계산 (Duration 활용)
        String workingTime = "-";
        if (att != null && att.getCommuteAt() != null && att.getLeaveAt() != null) {
            long minutes = Duration.between(att.getCommuteAt(), att.getLeaveAt()).toMinutes();
            workingTime = String.format("%dh %02dm", minutes / 60, minutes % 60);
        }

        return AdminAttendanceResDto.builder()
                .attendanceId(att != null ? att.getId() : null)
                .employeeName(emp.getName()) //
                .employeeNum(emp.getEmployeeNo()) //
                .workDate(date.toString())
                .commuteAt(att != null && att.getCommuteAt() != null
                        ? att.getCommuteAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                        : "-")
                .leaveAt(att != null && att.getLeaveAt() != null
                        ? att.getLeaveAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                        : "-")
                .workingTime(workingTime)
                .statusName(statusName)
                .statusCode(statusCode)
                .isCorrected(att != null && att.getIsCorrected())
                .correctionReason(att != null ? att.getCorrectionReason() : null)
                .build();
    }

    /**
     * [관리자] 기록 수동 정정 (안정성 강화)
     */
    @Transactional
    public void updateAttendanceRecord(Long attendanceId, Long companyId, AttendanceUpdateDto dto) {
        Attendance attendance;

        if (attendanceId != null && attendanceId > 0) {
            attendance = commuteRepository.findById(attendanceId)
                    .orElseThrow(() -> new RuntimeException("근태 기록을 찾을 수 없습니다."));
        } else {
            attendance = commuteRepository.findByCompanyIdAndEmployeeIdAndWorkDate(
                    companyId, dto.getEmployeeId(), LocalDate.now()
            ).orElseGet(() -> Attendance.builder()
                    .employeeId(dto.getEmployeeId())
                    .companyId(companyId)
                    .workDate(LocalDate.now())
                    .status(AttendanceStatus.BEFORE_WORK) // ✅ 신규 생성 시 status null 방지 (nullable=false)
                    .isCorrected(false)
                    .build());
        }

        attendance.updateStatus(AttendanceStatus.valueOf(dto.getStatus()), dto.getCorrectionReason());

        LocalDateTime commuteTime = (dto.getCommuteAt() != null && !dto.getCommuteAt().isBlank())
                ? parseDateTime(attendance.getWorkDate(), dto.getCommuteAt())
                : null;
        LocalDateTime leaveTime = (dto.getLeaveAt() != null && !dto.getLeaveAt().isBlank())
                ? parseDateTime(attendance.getWorkDate(), dto.getLeaveAt())
                : null;

        attendance.updateTimeByAdmin(commuteTime, leaveTime);

        commuteRepository.save(attendance);

        // 🚀 [알림 전송] 근태 정정 알림
        try {
            String message = String.format("[%s] 근태 상태가 '%s'(으)로 정정되었습니다. \n사유: %s",
                    attendance.getWorkDate(),
                    attendance.getStatus().getDescription(),
                    dto.getCorrectionReason());

            notificationCommandService.createNotification(
                    companyId,
                    attendance.getEmployeeId(),
                    com.finalproj.orbitflow.notification.enums.NotificationType.ATTENDANCE,
                    message,
                    "/view/attendance/monthly");
        } catch (Exception e) {
            // 알림 전송 실패가 트랜잭션 전체를 롤백시키지 않도록 로깅만 함
            // log.error("알림 전송 실패", e);
        }
    }

    private LocalDateTime parseDateTime(LocalDate date, String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty() || "-".equals(timeStr))
            return null;
        try {
            String fullTimeStr = timeStr.trim();
            if (fullTimeStr.length() == 5)
                fullTimeStr += ":00";
            return LocalDateTime.parse(date.toString() + " " + fullTimeStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }
}