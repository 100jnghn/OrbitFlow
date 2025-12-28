package com.finalproj.orbitflow.attendance.dashboard.service;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRepository;
import com.finalproj.orbitflow.attendance.dashboard.dto.AdminAttendanceResDto;
import com.finalproj.orbitflow.attendance.dashboard.dto.AdminSummaryResDto;
import com.finalproj.orbitflow.attendance.dashboard.dto.AttendanceUpdateDto;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // ✅ 정확한 임포트 확인
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration; // ✅ 근무 시간 계산을 위해 추가
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceDashboardService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public Page<AdminAttendanceResDto> getCompanyAttendanceList(
            Long companyId, String start, String end, String status, String keyword, Pageable pageable) {

        // 1. 기간 설정: 시작일만 있으면 당일, 시작/종료일 모두 있으면 해당 범위
        LocalDate startDate = (start == null || start.isEmpty()) ? LocalDate.now() : LocalDate.parse(start);
        LocalDate endDate = (end == null || end.isEmpty()) ? startDate : LocalDate.parse(end);

        AttendanceStatus statusEnum = (status == null || status.equals("ALL")) ? null : AttendanceStatus.valueOf(status);

        // 2. 수정된 리포지토리 메서드 호출 (인자 5개 + pageable)
        return attendanceRepository.findAllEmployeesWithAttendance(
                        companyId, startDate, endDate, statusEnum, keyword, pageable)
                .map(result -> {
                    Employee emp = (Employee) result[0];
                    Attendance att = (Attendance) result[1];
                    // 기록이 있으면 기록 날짜, 없으면 조회 시작일로 표시
                    LocalDate recordDate = (att != null) ? att.getWorkDate() : startDate;
                    return convertToCombinedDto(emp, att, recordDate);
                });
    }
    /**
     * [관리자] 상단 요약 통계 (전체 인원 대비 출근 현황)
     */
    public AdminSummaryResDto getTodaySummary(Long companyId) {
        LocalDate today = LocalDate.now();

        // 1. 전체 재직 인원 (사원 엔티티의 status 필드 기준)
        int totalActive = employeeRepository.countByCompanyIdAndStatus(companyId, EmployeeStatus.ACTIVE);

        // 2. 출근 완료 및 지각 카운트
        int onTime = attendanceRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today, AttendanceStatus.ON_TIME);
        int late = attendanceRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today, AttendanceStatus.LATE);

        // 3. 퇴근 미처리 (근무 중)
        int notLeaving = attendanceRepository.countByCompanyIdAndWorkDateAndLeaveAtIsNull(companyId, today);

        return AdminSummaryResDto.builder()
                .totalEmployees(totalActive)
                .onTimeCount(onTime + late)
                .lateCount(late)
                .notLeavingCount(notLeaving)
                .build();
    }

    /**
     * 엔티티 결합 및 DTO 변환 (근무 시간 계산 로직 추가)
     */
    private AdminAttendanceResDto convertToCombinedDto(Employee emp, Attendance att, LocalDate date) {
        // 출근 기록이 없는 경우: 오늘 날짜면 "근무예정", 과거 날짜면 "기록 누락"
        String statusName;
        String statusCode;
        if (att != null) {
            statusName = att.getStatus().getDescription();
            statusCode = att.getStatus().name();
        } else {
            // 오늘 날짜이고 출근 기록이 없으면 "근무예정", 과거 날짜면 "기록 누락"
            if (date.equals(LocalDate.now())) {
                statusName = AttendanceStatus.BEFORE_WORK.getDescription();
                statusCode = AttendanceStatus.BEFORE_WORK.name();
            } else {
                statusName = "기록 누락";
                statusCode = "ABSENT";
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
                .commuteAt(att != null && att.getCommuteAt() != null ?
                        att.getCommuteAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-")
                .leaveAt(att != null && att.getLeaveAt() != null ?
                        att.getLeaveAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-")
                .workingTime(workingTime)
                .statusName(statusName)
                .statusCode(statusCode)
                .isCorrected(att != null && att.getIsCorrected())
                .build();
    }

    /**
     * [관리자] 기록 수동 정정 (안정성 강화)
     */
    @Transactional
    public void updateAttendanceRecord(Long attendanceId, Long companyId, AttendanceUpdateDto dto) {
        Attendance attendance;

        if (attendanceId != null && attendanceId > 0) {
            attendance = attendanceRepository.findById(attendanceId)
                    .orElseThrow(() -> new RuntimeException("근태 기록을 찾을 수 없습니다."));
        } else {
            // "기록 누락" 사원의 경우 중복 생성 방지를 위해 재조회 후 생성
            attendance = attendanceRepository.findByCompanyIdAndEmployeeIdAndWorkDate(
                    companyId, dto.getEmployeeId(), LocalDate.now()).orElseGet(() ->
                    Attendance.builder()
                            .employeeId(dto.getEmployeeId())
                            .companyId(companyId)
                            .workDate(LocalDate.now())
                            .isCorrected(true)
                            .build()
            );
        }

        // 상태 및 사유 업데이트
        attendance.updateStatus(AttendanceStatus.valueOf(dto.getStatus()), dto.getCorrectionReason());

        // 시간 정보 수동 입력 처리
        if (dto.getCommuteAt() != null && !dto.getCommuteAt().isBlank()) {
            attendance.setCommuteAt(parseDateTime(attendance.getWorkDate(), dto.getCommuteAt()));
        }
        if (dto.getLeaveAt() != null && !dto.getLeaveAt().isBlank()) {
            attendance.setLeaveAt(parseDateTime(attendance.getWorkDate(), dto.getLeaveAt()));
        }

        attendanceRepository.save(attendance);
    }

    private LocalDateTime parseDateTime(LocalDate date, String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty() || "-".equals(timeStr)) return null;
        try {
            String fullTimeStr = timeStr.trim();
            if (fullTimeStr.length() == 5) fullTimeStr += ":00";
            return LocalDateTime.parse(date.toString() + " " + fullTimeStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }
}