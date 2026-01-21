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

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceDashboardService
 * @since : 2025. 12. 22. 월요일
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceDashboardService {

    private final CommuteRepository commuteRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRuleRepository attendanceRuleRepository;
    private final NotificationCommandService notificationCommandService;


    @Transactional(readOnly = true)
    public AdminSummaryResDto getTodaySummary(Long companyId) {
        LocalDate today = LocalDate.now();

        List<Employee> activeEmployees = employeeRepository.findByCompanyIdAndStatus(companyId, EmployeeStatus.ACTIVE);
        int totalActive = activeEmployees.size();

        Map<WorkStatus, Long> statusCounts = activeEmployees.stream()
                .collect(Collectors.groupingBy(Employee::getWorkStatus, Collectors.counting()));


        int vacation = statusCounts.getOrDefault(WorkStatus.VACATION, 0L).intValue();
        int outside = statusCounts.getOrDefault(WorkStatus.OUTWORK, 0L).intValue();
        int businessTrip = statusCounts.getOrDefault(WorkStatus.BUSINESS_TRIP, 0L).intValue();


        int onTimeRecord = commuteRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today,
                AttendanceStatus.ON_TIME);
        int lateRecord = commuteRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today,
                AttendanceStatus.LATE);

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

        LocalDate startDate = (start == null || start.isEmpty()) ? LocalDate.now() : LocalDate.parse(start);
        LocalDate endDate = (end == null || end.isEmpty()) ? startDate : LocalDate.parse(end);

        AttendanceStatus statusEnum = (status == null || status.equals("ALL")) ? null
                : AttendanceStatus.valueOf(status);

        AttendanceRule defaultRule = attendanceRuleRepository
                .findByCompanyIdAndIsDefaultTrue(companyId)
                .orElse(null);

        WorkStatus targetWorkStatus = null;
        if (statusEnum != null) {
            switch (statusEnum) {
                case VACATION -> targetWorkStatus = WorkStatus.VACATION;
                case BUSINESS_TRIP -> targetWorkStatus = WorkStatus.BUSINESS_TRIP;
                case OUTSIDE -> targetWorkStatus = WorkStatus.OUTWORK;
            }
        }

        return commuteRepository.findAllEmployeesWithAttendance(
                companyId, startDate, endDate, statusEnum, targetWorkStatus, keyword, pageable)
                .map(result -> {
                    Employee emp = (Employee) result[0];
                    Attendance att = (Attendance) result[1];
                    LocalDate recordDate = (att != null) ? att.getWorkDate() : startDate;
                    return convertToCombinedDto(emp, att, recordDate, defaultRule);
                });
    }

    /**
     * 엔티티 결합 및 DTO 변환 (근무 시간 계산 로직 추가)
     */
    private AdminAttendanceResDto convertToCombinedDto(Employee emp, Attendance att, LocalDate date,
            AttendanceRule defaultRule) {

        String statusName;
        String statusCode;
        if (att != null) {
            statusName = att.getStatus().getDescription();
            statusCode = att.getStatus().name();
        } else {

            if (date.equals(LocalDate.now())) {

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

                statusName = AttendanceStatus.ABSENT.getDescription();
                statusCode = AttendanceStatus.ABSENT.name();
            }
        }

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
                    .status(AttendanceStatus.BEFORE_WORK)
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