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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * [관리자] 전사 직원 근태 목록 조회 (전 사원 명단 포함)
     */
    public Page<AdminAttendanceResDto> getCompanyAttendanceList(
            Long companyId, String start, String end, String status, Pageable pageable) {

        // 날짜 필터가 없으면 오늘 기준
        LocalDate targetDate = (start == null || start.isEmpty()) ? LocalDate.now() : LocalDate.parse(start);

        // 검색 필터 (ALL이면 null 처리)
        AttendanceStatus statusEnum = (status == null || status.equals("ALL")) ? null : AttendanceStatus.valueOf(status);

        // Employee와 Attendance가 묶인 결과 페이징 조회
        return attendanceRepository.findAllEmployeesWithAttendance(companyId, targetDate, statusEnum, pageable)
                .map(result -> {
                    Employee emp = (Employee) result[0]; //
                    Attendance att = (Attendance) result[1]; //
                    return convertToCombinedDto(emp, att, targetDate);
                });
    }
    /**document_signature
     * [관리자] 상단 요약 통계
     */
    public AdminSummaryResDto getTodaySummary(Long companyId) {
        LocalDate today = LocalDate.now();

        // 1. 전체 재직 인원 (분모)
        int totalActive = employeeRepository.countByCompanyIdAndStatus(companyId, EmployeeStatus.ACTIVE);

        // 2. 출근 완료 (정상 + 지각 인원)
        int onTime = attendanceRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today, AttendanceStatus.ON_TIME);
        int late = attendanceRepository.countByCompanyIdAndWorkDateAndStatus(companyId, today, AttendanceStatus.LATE);

        // 3. 퇴근 미처리 (출근 기록은 있으나 퇴근 시각이 없는 경우)
        int notLeaving = attendanceRepository.countByCompanyIdAndWorkDateAndLeaveAtIsNull(companyId, today);

        return AdminSummaryResDto.builder()
                .totalEmployees(totalActive)
                .onTimeCount(onTime + late) // 합산하여 표시
                .lateCount(late)
                .notLeavingCount(notLeaving)
                .build();
    }
    private AdminAttendanceResDto convertToCombinedDto(Employee emp, Attendance att, LocalDate date) {
        // 출근 기록이 없으면 "기록 누락(ABSENT)"으로 가공
        String statusName = (att != null) ? att.getStatus().getDescription() : "기록 누락";
        String statusCode = (att != null) ? att.getStatus().name() : "ABSENT";

        return AdminAttendanceResDto.builder()
                .attendanceId(att != null ? att.getId() : null)
                .employeeName(emp.getName()) //
                .employeeNum(emp.getEmployeeNo()) //
                .workDate(date.toString())
                .commuteAt(att != null && att.getCommuteAt() != null ?
                        att.getCommuteAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-")
                .leaveAt(att != null && att.getLeaveAt() != null ?
                        att.getLeaveAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-")
                .statusName(statusName)
                .statusCode(statusCode)
                .isCorrected(att != null && att.getIsCorrected())
                .build();
    }


    /**
     * [관리자] 직원 근태 기록 수동 정정 (레코드 생성 로직 추가)
     */
    @Transactional
    public void updateAttendanceRecord(Long attendanceId, Long companyId, AttendanceUpdateDto dto) {
        Attendance attendance;

        if (attendanceId != null && attendanceId > 0) {
            // 1. 기존 기록 수정
            attendance = attendanceRepository.findById(attendanceId)
                    .orElseThrow(() -> new RuntimeException("근태 기록을 찾을 수 없습니다."));
        } else {
            // 2. 기록 누락자 신규 생성 (DTO에 employeeId가 포함되어 있어야 함)
            // 만약 오늘 날짜에 이미 기록이 생겼는지 한 번 더 체크 (중복 방지)
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
        
        AttendanceStatus newStatus = AttendanceStatus.valueOf(dto.getStatus());
        attendance.updateStatus(newStatus, dto.getCorrectionReason());

        // 4. 출퇴근 시간도 함께 정정하는 경우 (선택 사항)
        if (dto.getCommuteAt() != null && !dto.getCommuteAt().isEmpty()) {
            attendance.setCommuteAt(parseDateTime(attendance.getWorkDate(), dto.getCommuteAt()));
        }
        if (dto.getLeaveAt() != null && !dto.getLeaveAt().isEmpty()) {
            attendance.setLeaveAt(parseDateTime(attendance.getWorkDate(), dto.getLeaveAt()));
        }

        attendanceRepository.save(attendance);
    }


    private LocalDateTime parseDateTime(LocalDate date, String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty() || "-".equals(timeStr)) {
            return null;
        }

        try {
            // 초 정보가 없는 "HH:mm" 형식일 경우 ":00"을 붙여서 처리
            String fullTimeStr = timeStr.trim();
            if (fullTimeStr.length() == 5) { // 예: 09:00
                fullTimeStr += ":00";
            }

            // 날짜(yyyy-MM-dd)와 시간(HH:mm:ss)을 결합
            String dateTimeStr = date.toString() + " " + fullTimeStr;
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            // 파싱 실패 시 로그를 남기고 null 반환 (에러 발생 방지)
            System.err.println("시간 파싱 에러 [" + timeStr + "]: " + e.getMessage());
            return null;
        }
    }

}