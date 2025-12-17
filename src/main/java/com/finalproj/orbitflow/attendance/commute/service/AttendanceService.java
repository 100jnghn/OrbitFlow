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
        // 엔티티의 uniqueConstraints인 {employee_id, work_date}를 기준으로 조회
        Optional<Attendance> attendance = attendanceRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, LocalDate.now());

        return attendance.map(AttendanceDto.TodayAttendanceResponse::new)
                .orElseGet(AttendanceDto.TodayAttendanceResponse::new);
    }
//
//    /**
//     * 출근 처리: 예외 규칙 -> 기본 규칙 순으로 기준 시간 적용
//     */
//    @Transactional
//    public AttendanceDto.TodayAttendanceResponse checkIn(Long companyId, Long employeeId) {
//        LocalDate today = LocalDate.now();
//        LocalDateTime now = LocalDateTime.now();
//
//        // 1. 중복 출근 체크 (uniqueConstraints 위반 방지)
//        attendanceRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, today)
//                .ifPresent(a -> {
//                    if (a.getCommuteAt() != null) throw new IllegalStateException("이미 출근 처리되었습니다.");
//                });
//
//        // 2. 적용할 출근 기준 시간 결정 (사원별 예외 규칙 우선)
//        LocalTime startTimeThreshold = getApplicableStartTime(companyId, employeeId, today);
//
//        // 3. 기록 생성 및 지각 판별
//        Attendance attendance = new Attendance();
//        attendance.setCompanyId(companyId);
//        attendance.setEmployeeId(employeeId);
//        attendance.setWorkDate(today);
//        attendance.setCommuteAt(now);
//        attendance.setIsCorrected(false); // 기본값 설정
//
//        // 기준 시간보다 늦게 출근하면 LATE(지각), 아니면 ON_TIME(정상출근)
//        AttendanceStatus status = now.toLocalTime().isAfter(startTimeThreshold)
//                ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME;
//        attendance.setStatus(status); // Enum 타입으로 저장
//
//        return new AttendanceDto.TodayAttendanceResponse(attendanceRepository.save(attendance));
//    }

    /**
     * 퇴근 처리: 예외 규칙 -> 기본 규칙 순으로 기준 시간 적용
     */
    @Transactional
    public AttendanceDto.TodayAttendanceResponse checkOut(Long companyId, Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 1. 오늘 출근 기록이 있는지 확인
        Attendance attendance = attendanceRepository.findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, today)
                .orElseThrow(() -> new IllegalStateException("출근 기록이 없습니다. 먼저 출근해주세요."));
//
//        if (attendance.getLeaveAt() != null) throw new IllegalStateException("이미 퇴근 처리되었습니다.");
//
//        // 2. 적용할 퇴근 기준 시간 결정
//        LocalTime endTimeThreshold = getApplicableEndTime(companyId, employeeId, today);
//
//        // 3. 퇴근 기록 및 조퇴 판별
//        attendance.setLeaveAt(now);
//
//        // 퇴근 기준 시간보다 일찍 퇴근하면 EARLY_LEAVE(조퇴)
//        if (now.toLocalTime().isBefore(endTimeThreshold)) {
//            attendance.setStatus(AttendanceStatus.EARLY_LEAVE); // Enum 타입 업데이트
//        }

        return new AttendanceDto.TodayAttendanceResponse(attendanceRepository.save(attendance));
    }

//    /**
//     * [Helper] 사원 예외 규칙 -> 회사 기본 규칙 순으로 출근 기준 시간 조회
//     */
//    private LocalTime getApplicableStartTime(Long companyId, Long employeeId, LocalDate date) {
//        return employeeAttRuleRepository.findActiveRuleByEmployeeIdAndDate(employeeId, date)
//                .map(EmployeeAttRule::getStartTime)
//                .orElseGet(() ->
//                        attendanceRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
//                                .map(AttendanceRule::getDefaultStartTime)
//                                .orElse(LocalTime.of(9, 0)) // 기본 규칙 없으면 09:00 적용
//                );
//    }
//
//    /**
//     * [Helper] 사원 예외 규칙 -> 회사 기본 규칙 순으로 퇴근 기준 시간 조회
//     */
//    private LocalTime getApplicableEndTime(Long companyId, Long employeeId, LocalDate date) {
//        return employeeAttRuleRepository.findActiveRuleByEmployeeIdAndDate(employeeId, date)
//                .map(EmployeeAttRule::getEndTime)
//                .orElseGet(() ->
//                        attendanceRuleRepository.findByCompanyIdAndIsDefaultTrue(companyId)
//                                .map(AttendanceRule::getDefaultEndTime)
//                                .orElse(LocalTime.of(18, 0)) // 기본 규칙 없으면 18:00 적용
//                );
//    }
}