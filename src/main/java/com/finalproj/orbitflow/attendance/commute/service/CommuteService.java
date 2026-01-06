package com.finalproj.orbitflow.attendance.commute.service;

import com.finalproj.orbitflow.attendance.commute.dto.ActiveRuleResDto;
import com.finalproj.orbitflow.attendance.commute.dto.TodayAttResDto;
import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.commute.repository.CommuteRepository;
import com.finalproj.orbitflow.attendance.rule.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.attendance.rule.repository.EmployeeRuleRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommuteService {

    private final CommuteRepository commuteRepository;
    private final AttendanceRuleRepository attendanceRuleRepository;
    private final EmployeeRuleRepository employeeRuleRepository;
    private final EmployeeRepository employeeRepository;


    public ActiveRuleResDto getActiveRule(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();

        return employeeRuleRepository.findActiveRuleByEmployeeIdAndDate(employeeId, today)
                .map(rule -> new ActiveRuleResDto(rule.getStartTime(), rule.getEndTime(), "EXCEPTION"))
                .orElseGet(() -> attendanceRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
                        .map(rule -> new ActiveRuleResDto(rule.getDefaultStartTime(), rule.getDefaultEndTime(), "DEFAULT"))
                        .orElse(new ActiveRuleResDto(LocalTime.of(9, 0), LocalTime.of(18, 0), "SYSTEM")));
    }


    public TodayAttResDto getTodayAttendance(Long companyId, Long employeeId) {
        Attendance attendance = commuteRepository.findToday(companyId, employeeId).orElse(null);
        Employee employee = findEmployee(employeeId);

        return new TodayAttResDto(attendance, employee.getWorkStatus() == WorkStatus.AWAY);
    }


    @Transactional
    public TodayAttResDto checkIn(Long companyId, Long employeeId) {
        // 이미 출근했는지 검증
        commuteRepository.findToday(companyId, employeeId)
                .ifPresent(a -> { throw new BusinessException("이미 출근 처리되었습니다."); });

        ActiveRuleResDto rule = getActiveRule(companyId, employeeId);
        LocalDateTime now = LocalDateTime.now();

        Attendance attendance = Attendance.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .workDate(now.toLocalDate())
                .commuteAt(now)
                .isCorrected(false)
                .status(determineAttendanceStatus(now.toLocalTime(), rule.getStartTime()))
                .build();

        updateEmployeeWorkStatus(employeeId, WorkStatus.WORKING);
        return new TodayAttResDto(commuteRepository.save(attendance), false);
    }


    @Transactional
    public TodayAttResDto checkOut(Long companyId, Long employeeId) {
        Attendance attendance = commuteRepository.findToday(companyId, employeeId)
                .orElseThrow(() -> new BusinessException("출근 기록이 없습니다."));

        if (attendance.getLeaveAt() != null) {
            throw new BusinessException("이미 퇴근 처리되었습니다.");
        }

        ActiveRuleResDto rule = getActiveRule(companyId, employeeId);
        validateCheckOutTime(rule.getEndTime());

        attendance.setLeaveAt(LocalDateTime.now());
        updateEmployeeWorkStatus(employeeId, WorkStatus.OFF_WORK);

        return new TodayAttResDto(commuteRepository.save(attendance), false);
    }


    @Transactional
    public void startAway(Long companyId, Long employeeId) {
        validateAttendanceRecord(companyId, employeeId);

        Employee employee = findEmployee(employeeId);
        if (employee.getWorkStatus() == WorkStatus.AWAY) {
            throw new BusinessException("이미 자리비움 상태입니다.");
        }

        employee.updateWorkStatus(WorkStatus.AWAY);
    }


    @Transactional
    public void endAway(Long companyId, Long employeeId) {
        updateEmployeeWorkStatus(employeeId, WorkStatus.WORKING);
    }


    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("사원 정보를 찾을 수 없습니다."));
    }

    private void updateEmployeeWorkStatus(Long employeeId, WorkStatus status) {
        findEmployee(employeeId).updateWorkStatus(status);
    }

    private void validateAttendanceRecord(Long companyId, Long employeeId) {
        commuteRepository.findToday(companyId, employeeId)
                .orElseThrow(() -> new BusinessException("출근 기록이 없습니다. 자리비움은 출근 후에 가능합니다."));
    }

    private AttendanceStatus determineAttendanceStatus(LocalTime now, LocalTime startTimeThreshold) {
        return now.withNano(0).isAfter(startTimeThreshold.withNano(0))
                ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME;
    }

    private void validateCheckOutTime(LocalTime endTimeThreshold) {
        LocalTime now = LocalTime.now().withNano(0);
        if (now.isBefore(endTimeThreshold.withNano(0))) {
            throw new BusinessException("퇴근 시간(" + endTimeThreshold + ") 이전이라서 퇴근할 수 없습니다.");
        }
    }

    // 조직도 페이지 - 사원 근무 상태 조회
    @Transactional(readOnly = true)
    public WorkStatus getEmployeeWorkStatus(Long companyId, Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("사원 정보를 찾을 수 없습니다."));

        if (!employee.getCompany().getId().equals(companyId)) {
            throw new BusinessException("다른 회사의 사원입니다.");
        }

        return employee.getWorkStatus();
    }

}