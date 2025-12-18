package com.finalproj.orbitflow.attendance.attendance.service;

import com.finalproj.orbitflow.attendance.attendance.dto.TodayAttResDto; // 변경된 DTO 임포트
import com.finalproj.orbitflow.attendance.attendance.entity.Attendance;
import com.finalproj.orbitflow.attendance.attendanceRule.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.employeeAttRule.entity.EmployeeAttRule;
import com.finalproj.orbitflow.attendance.attendance.enums.AttendanceStatus;
import com.finalproj.orbitflow.attendance.attendance.repository.AttendanceRepository;
import com.finalproj.orbitflow.attendance.attendanceRule.repository.AttendanceRuleRepository;
import com.finalproj.orbitflow.attendance.employeeAttRule.repository.EmployeeAttRuleRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.WorkStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
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
    private final EmployeeRepository employeeRepository;

    /**
     * 오늘의 근태 및 실시간 상태 조회
     */
    public TodayAttResDto getTodayAttendance(Long companyId, Long employeeId) {
        // 1. 오늘의 출퇴근 기록 조회
        Optional<Attendance> attendance = attendanceRepository.findToday(companyId, employeeId);

        // 2. 사원의 실시간 자리비움 여부 확인
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        boolean isAway = employee != null && employee.getWorkStatus() == WorkStatus.AWAY;

        return new TodayAttResDto(attendance.orElse(null), isAway);
    }

    /**
     * 출근 처리 (지각/정상 판정)
     */
    @Transactional
    public TodayAttResDto checkIn(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 1. 중복 출근 체크
        attendanceRepository.findToday(companyId, employeeId)
                .ifPresent(a -> { throw new IllegalStateException("이미 출근 처리되었습니다."); });

        // 2. 기준 시간 결정 (지각 판정용)
        LocalTime startTimeThreshold = getApplicableStartTime(companyId, employeeId, today);

        // 3. 근태 기록 생성 및 지각(LATE) / 정상출근(ON_TIME) 판정
        Attendance attendance = new Attendance();
        attendance.setCompanyId(companyId);
        attendance.setEmployeeId(employeeId);
        attendance.setWorkDate(today);
        attendance.setCommuteAt(now);
        attendance.setIsCorrected(false);

        // 나노초 오차 방지를 위해 초 단위 비교
        AttendanceStatus status = now.toLocalTime().withNano(0).isAfter(startTimeThreshold)
                ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME;
        attendance.setStatus(status);

        // 4. 사원의 실시간 상태 업데이트 -> WORKING
        updateEmployeeWorkStatus(employeeId, WorkStatus.WORKING);

        return new TodayAttResDto(attendanceRepository.save(attendance), false);
    }

    /**
     * 퇴근 처리 (조퇴 판정)
     */
    @Transactional
    public TodayAttResDto checkOut(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Attendance attendance = attendanceRepository.findToday(companyId, employeeId)
                .orElseThrow(() -> new IllegalStateException("출근 기록이 없습니다."));

        if (attendance.getLeaveAt() != null) throw new IllegalStateException("이미 퇴근 처리되었습니다.");

        LocalTime endTimeThreshold = getApplicableEndTime(companyId, employeeId, today);

        // 5. 퇴근 기록 및 조퇴(EARLY_LEAVE) 판정
        attendance.setLeaveAt(now);
        if (now.toLocalTime().isBefore(endTimeThreshold)) {
            attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
        }

        // 6. 사원의 실시간 상태 업데이트 -> OFF_WORK
        updateEmployeeWorkStatus(employeeId, WorkStatus.OFF_WORK);

        return new TodayAttResDto(attendanceRepository.save(attendance), false);
    }

    /**
     * 자리비움 시작
     */
    @Transactional
    public void startAway(Long companyId, Long employeeId) {
        attendanceRepository.findToday(companyId, employeeId)
                .orElseThrow(() -> new IllegalStateException("출근 기록이 없습니다."));

        updateEmployeeWorkStatus(employeeId, WorkStatus.AWAY);
    }

    /**
     * 자리비움 종료
     */
    @Transactional
    public void endAway(Long companyId, Long employeeId) {
        updateEmployeeWorkStatus(employeeId, WorkStatus.WORKING);
    }

    private void updateEmployeeWorkStatus(Long employeeId, WorkStatus status) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalStateException("사원 정보를 찾을 수 없습니다."));
        employee.updateWorkStatus(status);
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