package com.finalproj.orbitflow.attendance.commute.service;

import com.finalproj.orbitflow.attendance.commute.dto.ActiveRuleResDto;
import com.finalproj.orbitflow.attendance.commute.dto.TodayAttResDto;
import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.default_rule.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.exception_rule.entity.EmployeeAttRule;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.AttendanceRepository;
import com.finalproj.orbitflow.attendance.default_rule.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.attendance.exception_rule.repository.EmployeeAttRuleRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.WorkStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.global.exception.BusinessException; // 전역 예외 처리기와 연동
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
public class CommuteService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceRuleRepository attendanceRuleRepository;
    private final EmployeeAttRuleRepository employeeAttRuleRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public ActiveRuleResDto getActiveRule(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();
        return employeeAttRuleRepository.findActiveRuleByEmployeeIdAndDate(employeeId, today)
                .map(exceptionRule -> new ActiveRuleResDto(
                        exceptionRule.getStartTime(),
                        exceptionRule.getEndTime(),
                        "EXCEPTION"
                ))
                .orElseGet(() -> attendanceRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
                        .map(defaultRule -> new ActiveRuleResDto(
                                defaultRule.getDefaultStartTime(),
                                defaultRule.getDefaultEndTime(),
                                "DEFAULT"
                        ))
                        .orElse(new ActiveRuleResDto(LocalTime.of(9, 0), LocalTime.of(18, 0), "SYSTEM")));
    }

    public TodayAttResDto getTodayAttendance(Long companyId, Long employeeId) {
        Optional<Attendance> attendance = attendanceRepository.findToday(companyId, employeeId);
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        boolean isAway = employee != null && employee.getWorkStatus() == WorkStatus.AWAY;
        return new TodayAttResDto(attendance.orElse(null), isAway);
    }

    @Transactional
    public TodayAttResDto checkIn(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        attendanceRepository.findToday(companyId, employeeId)
                .ifPresent(a -> { throw new BusinessException("이미 출근 처리되었습니다."); });

        LocalTime startTimeThreshold = getApplicableStartTime(companyId, employeeId, today);

        Attendance attendance = new Attendance();
        attendance.setCompanyId(companyId);
        attendance.setEmployeeId(employeeId);
        attendance.setWorkDate(today);
        attendance.setCommuteAt(now);
        attendance.setIsCorrected(false);

        AttendanceStatus status = now.toLocalTime().withNano(0).isAfter(startTimeThreshold)
                ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME;
        attendance.setStatus(status);

        updateEmployeeWorkStatus(employeeId, WorkStatus.WORKING);
        return new TodayAttResDto(attendanceRepository.save(attendance), false);
    }

    /**
     * 퇴근 처리 수정
     * ✅ IllegalStateException 대신 BusinessException을 던져 팝업 메시지 출력 유도
     */
    @Transactional
    public TodayAttResDto checkOut(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Attendance attendance = attendanceRepository.findToday(companyId, employeeId)
                .orElseThrow(() -> new BusinessException("출근 기록이 없습니다."));

        if (attendance.getLeaveAt() != null) {
            throw new BusinessException("이미 퇴근 처리되었습니다.");
        }

        LocalTime endTimeThreshold = getApplicableEndTime(companyId, employeeId, today);
        LocalTime nowTime = now.toLocalTime().withNano(0);
        LocalTime thresholdTime = endTimeThreshold.withNano(0);

        // ✅ 핵심: 퇴근 시간 이전 체크 시 BusinessException 사용
        if (nowTime.isBefore(thresholdTime)) {
            throw new BusinessException(
                    "퇴근 시간(" + thresholdTime + ") 이전이라서 퇴근할 수 없습니다."
            );
        }

        attendance.setLeaveAt(now);
        updateEmployeeWorkStatus(employeeId, WorkStatus.OFF_WORK);
        return new TodayAttResDto(attendanceRepository.save(attendance), false);
    }

    @Transactional
    public void startAway(Long companyId, Long employeeId) {
        attendanceRepository.findToday(companyId, employeeId)
                .orElseThrow(() -> new BusinessException("출근 기록이 없습니다."));
        updateEmployeeWorkStatus(employeeId, WorkStatus.AWAY);
    }

    @Transactional
    public void endAway(Long companyId, Long employeeId) {
        updateEmployeeWorkStatus(employeeId, WorkStatus.WORKING);
    }

    private void updateEmployeeWorkStatus(Long employeeId, WorkStatus status) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("사원 정보를 찾을 수 없습니다."));
        employee.updateWorkStatus(status);
    }

    public LocalTime getApplicableStartTime(Long companyId, Long employeeId, LocalDate date) {
        return employeeAttRuleRepository.findActiveRuleByEmployeeIdAndDate(employeeId, date)
                .map(EmployeeAttRule::getStartTime)
                .orElseGet(() -> attendanceRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
                        .map(AttendanceRule::getDefaultStartTime)
                        .orElse(LocalTime.of(9, 0)));
    }

    public LocalTime getApplicableEndTime(Long companyId, Long employeeId, LocalDate date) {
        return employeeAttRuleRepository.findActiveRuleByEmployeeIdAndDate(employeeId, date)
                .map(EmployeeAttRule::getEndTime)
                .orElseGet(() -> attendanceRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
                        .map(AttendanceRule::getDefaultEndTime)
                        .orElse(LocalTime.of(18, 0)));
    }
}