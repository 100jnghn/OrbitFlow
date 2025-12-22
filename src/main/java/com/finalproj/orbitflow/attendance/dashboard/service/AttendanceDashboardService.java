package com.finalproj.orbitflow.attendance.dashboard.service;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRepository;
import com.finalproj.orbitflow.attendance.dashboard.dto.*;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceDashboardService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * [관리자] 전사 직원 근태 목록 조회 (페이징 및 필터)
     */
    @Transactional(readOnly = true)
    public Page<AdminAttendanceResDto> getCompanyAttendanceList(
            Long companyId, String start, String end, String status, Pageable pageable) {

        // 1. Repository에서 Page<Attendance> 조회
        Page<Attendance> attendancePage = attendanceRepository.findAllByCompanyIdAndFilters(companyId, start, end, status, pageable);

        // 2. 람다식을 사용하여 DTO로 변환 (this:: 에러 방지)
        return attendancePage.map(attendance -> convertToAdminDto(attendance));
    }

    /**
     * [관리자] 금일 근태 요약 통계 조회
     */
    public AdminSummaryResDto getTodaySummary(Long companyId) {
        LocalDate today = LocalDate.now();

        // ACTIVE(재직) 상태인 직원 수 조회
        int totalActive = employeeRepository.countByCompanyIdAndStatus(companyId, EmployeeStatus.ACTIVE);

        return AdminSummaryResDto.builder()
                .totalEmployees(totalActive)
                .onTimeCount(attendanceRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today, AttendanceStatus.ON_TIME))
                .lateCount(attendanceRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today, AttendanceStatus.LATE))
                .notLeavingCount(attendanceRepository.countByCompanyIdAndWorkDateAndLeaveAtIsNull(companyId, today))
                .pendingRequestCount(0)
                .build();
    }

    /**
     * [관리자] 직원 근태 기록 수동 정정
     */
    @Transactional
    public void updateAttendanceRecord(Long attendanceId, AttendanceUpdateDto dto) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("근태 기록을 찾을 수 없습니다."));

        // 상태 및 정정 사유 업데이트
        AttendanceStatus newStatus = AttendanceStatus.valueOf(dto.getStatus());
        attendance.updateStatus(newStatus, dto.getCorrectionReason());

        // 시각 수정 로직
        if (dto.getCommuteAt() != null) {
            attendance.setCommuteAt(parseDateTime(attendance.getWorkDate(), dto.getCommuteAt()));
        }
        if (dto.getLeaveAt() != null) {
            attendance.setLeaveAt(parseDateTime(attendance.getWorkDate(), dto.getLeaveAt()));
        }

        attendance.markAsCorrected();
    }

    /**
     * Entity(Attendance) -> DTO(AdminAttendanceResDto) 변환
     */
    private AdminAttendanceResDto convertToAdminDto(Attendance attendance) {
        // 사원 정보 조회 (이름, 사번)
        Employee emp = employeeRepository.findById(attendance.getEmployeeId()).orElse(null);
        String empName = (emp != null) ? emp.getName() : "탈퇴 사원";
        String empNo = (emp != null) ? emp.getEmployeeNo() : "-";

        // 근무 시간 계산
        String workingTime = "-";
        if (attendance.getCommuteAt() != null && attendance.getLeaveAt() != null) {
            Duration duration = Duration.between(attendance.getCommuteAt(), attendance.getLeaveAt());
            workingTime = String.format("%d시간 %d분", duration.toHours(), duration.toMinutesPart());
        } else if (attendance.getCommuteAt() != null) {
            workingTime = "근무 중";
        }

        return AdminAttendanceResDto.builder()
                .attendanceId(attendance.getId())
                .employeeName(empName)
                .employeeNum(empNo)
                .workDate(attendance.getWorkDate().toString())
                .commuteAt(formatTime(attendance.getCommuteAt()))
                .leaveAt(formatTime(attendance.getLeaveAt()))
                .workingTime(workingTime)
                .statusName(attendance.getStatus() != null ? attendance.getStatus().getDescription() : "-")
                .statusCode(attendance.getStatus() != null ? attendance.getStatus().name() : "")
                .isCorrected(attendance.getIsCorrected())
                .correctionReason(attendance.getCorrectionReason())
                .build();
    }

    private String formatTime(LocalDateTime dateTime) {
        return (dateTime != null) ? dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-";
    }

    private LocalDateTime parseDateTime(LocalDate date, String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            return LocalDateTime.parse(date.toString() + " " + timeStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }
}