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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceDashboardService {

    private final CommuteRepository commuteRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRuleRepository attendanceRuleRepository;
    private final NotificationCommandService notificationCommandService;

    // 요약 통계
    public AdminSummaryResDto getTodaySummary(Long companyId) {
        LocalDate today = LocalDate.now();

        // 1. 전체 재직 인원
        int totalActive = employeeRepository.countByCompanyIdAndStatus(companyId, EmployeeStatus.ACTIVE);

        // 2. 출근 기록 카운트
        int onTime = commuteRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today, AttendanceStatus.ON_TIME);
        int late = commuteRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today, AttendanceStatus.LATE);

        // 3. 기타 승인된 상태 카운트
        int vacation = commuteRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today,
                AttendanceStatus.VACATION);
        int outside = commuteRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today,
                AttendanceStatus.OUTSIDE);
        int businessTrip = commuteRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today,
                AttendanceStatus.BUSINESS_TRIP);

        // 4. 결근 인원 계산: 전체 - (출근자 + 휴가 + 외근 + 출장)
        int totalAccountedFor = (onTime + late + vacation + outside + businessTrip);
        int absentCount = totalActive - totalAccountedFor;

        return AdminSummaryResDto.builder()
                .totalEmployees(totalActive)
                .onTimeCount(onTime + late)
                .lateCount(late)
                .absentCount(Math.max(0, absentCount))
                .vacationCount(vacation)
                .outsideCount(outside)
                .businessTripCount(businessTrip)
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

        // 3. 수정된 리포지토리 메서드 호출 (인자 5개 + pageable)
        return commuteRepository.findAllEmployeesWithAttendance(
                companyId, startDate, endDate, statusEnum, keyword, pageable)
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
            if (date.equals(LocalDate.now()) && defaultRule != null) {
                // 오늘 날짜이고 기본 규칙이 있는 경우
                LocalTime now = LocalTime.now();
                LocalTime startTime = defaultRule.getDefaultStartTime();
                LocalTime endTime = defaultRule.getDefaultEndTime();

                // 현재 시간이 출근 시간 이전이면 "근무예정"
                if (now.isBefore(startTime)) {
                    statusName = AttendanceStatus.BEFORE_WORK.getDescription();
                    statusCode = AttendanceStatus.BEFORE_WORK.name();
                }
                // 현재 시간이 퇴근 시간 이후이면 "결근"
                else if (now.isAfter(endTime)) {
                    statusName = AttendanceStatus.ABSENT.getDescription();
                    statusCode = AttendanceStatus.ABSENT.name();
                }
                // 출근 시간 이후, 퇴근 시간 이전이면 "근무예정" (아직 출근할 수 있는 시간)
                else {
                    statusName = AttendanceStatus.BEFORE_WORK.getDescription();
                    statusCode = AttendanceStatus.BEFORE_WORK.name();
                }
            } else if (date.equals(LocalDate.now())) {
                // 오늘 날짜이지만 기본 규칙이 없는 경우 기본값으로 "근무예정"
                statusName = AttendanceStatus.BEFORE_WORK.getDescription();
                statusCode = AttendanceStatus.BEFORE_WORK.name();
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
                    companyId, dto.getEmployeeId(), LocalDate.now()).orElseGet(
                    () -> Attendance.builder()
                            .employeeId(dto.getEmployeeId())
                            .companyId(companyId)
                            .workDate(LocalDate.now())
                            .isCorrected(true)
                            .build());
        }


        attendance.updateStatus(AttendanceStatus.valueOf(dto.getStatus()), dto.getCorrectionReason());

        LocalDateTime commuteTime = (dto.getCommuteAt() != null && !dto.getCommuteAt().isBlank())
                ? parseDateTime(attendance.getWorkDate(), dto.getCommuteAt()) : null;
        LocalDateTime leaveTime = (dto.getLeaveAt() != null && !dto.getLeaveAt().isBlank())
                ? parseDateTime(attendance.getWorkDate(), dto.getLeaveAt()) : null;

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