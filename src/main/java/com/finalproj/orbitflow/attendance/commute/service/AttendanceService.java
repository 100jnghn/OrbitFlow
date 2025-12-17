package com.finalproj.orbitflow.attendance.commute.service;

import com.finalproj.orbitflow.attendance.commute.dto.AttendanceDto;
import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.commute.entity.EmployeeAttRule;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRepository;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.attendance.commute.repository.EmployeeAttRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceRuleRepository attendanceRuleRepository;
    private final EmployeeAttRuleRepository employeeAttRuleRepository;

    /**
     * 오늘의 출퇴근 기록 조회
     */
    public AttendanceDto.TodayAttendanceResponse getTodayAttendance(Long companyId, Long employeeId) {
        Optional<Attendance> attendance = attendanceRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, LocalDate.now());

        return attendance.map(AttendanceDto.TodayAttendanceResponse::new)
                .orElseGet(AttendanceDto.TodayAttendanceResponse::new);
    }

    /**
     * 출근 처리 (지각 판정 포함)
     */
    @Transactional
    public AttendanceDto.TodayAttendanceResponse checkIn(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 1. 중복 출근 체크
        attendanceRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, today)
                .ifPresent(a -> { throw new IllegalStateException("이미 출근 처리되었습니다."); });

        // 2. 기준 시간 결정 (예외 규칙 우선)
        LocalTime startTimeThreshold = getApplicableStartTime(companyId, employeeId, today);

        // 3. 기록 생성 및 저장
        Attendance attendance = new Attendance();
        attendance.setCompanyId(companyId);
        attendance.setEmployeeId(employeeId);
        attendance.setWorkDate(today);
        attendance.setCommuteAt(now);
        attendance.setIsCorrected(false);

        // 지각 여부 판별
        AttendanceStatus status = now.toLocalTime().isAfter(startTimeThreshold)
                ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME;
        attendance.setStatus(status);

        return new AttendanceDto.TodayAttendanceResponse(attendanceRepository.save(attendance));
    }

    /**
     * 퇴근 처리 (조퇴 판정 포함)
     */
    @Transactional
    public AttendanceDto.TodayAttendanceResponse checkOut(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Attendance attendance = attendanceRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, today)
                .orElseThrow(() -> new IllegalStateException("출근 기록이 없습니다."));

        if (attendance.getLeaveAt() != null) throw new IllegalStateException("이미 퇴근 처리되었습니다.");

        // 1. 기준 시간 결정
        LocalTime endTimeThreshold = getApplicableEndTime(companyId, employeeId, today);

        // 2. 퇴근 시각 기록 및 조퇴 판별
        attendance.setLeaveAt(now);
        if (now.toLocalTime().isBefore(endTimeThreshold)) {
            attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
        }

        return new AttendanceDto.TodayAttendanceResponse(attendanceRepository.save(attendance));
    }

    private LocalTime getApplicableStartTime(Long companyId, Long employeeId, LocalDate date) {
        return employeeAttRuleRepository.findActiveRuleByEmployeeIdAndDate(employeeId, date)
                .map(EmployeeAttRule::getStartTime)
                .orElseGet(() -> attendanceRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
                        .map(AttendanceRule::getDefaultStartTime)
                        .orElse(LocalTime.of(9, 0)));
    }

    private LocalTime getApplicableEndTime(Long companyId, Long employeeId, LocalDate date) {
        return employeeAttRuleRepository.findActiveRuleByEmployeeIdAndDate(employeeId, date)
                .map(EmployeeAttRule::getEndTime)
                .orElseGet(() -> attendanceRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
                        .map(AttendanceRule::getDefaultEndTime)
                        .orElse(LocalTime.of(18, 0)));
    }
}