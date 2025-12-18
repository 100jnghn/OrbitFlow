package com.finalproj.orbitflow.attendance.commute.controller;

import com.finalproj.orbitflow.attendance.commute.dto.AttendanceDto;
import com.finalproj.orbitflow.attendance.commute.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // TODO: 추후 세션이나 Spring Security의 Authentication 객체에서 가져오도록 수정 필요
    private final Long CURRENT_EMPLOYEE_ID = 1L;
    private final Long CURRENT_COMPANY_ID = 1L;

    /**
     * 오늘의 근태 현황 조회
     * (출근 전이면 "근무예정", 출근 후면 "정상출근/지각" 및 "자리비움 여부" 반환)
     */
    @GetMapping("/today")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> getTodayAttendance() {
        return ResponseEntity.ok(attendanceService.getTodayAttendance(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID));
    }

    /**
     * 출근 처리
     * 결과: 정상출근(ON_TIME) 또는 지각(LATE) 판정
     */
    @PostMapping("/checkin")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> checkIn() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.checkIn(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID));
    }

    /**
     * 퇴근 처리
     * 결과: 조퇴(EARLY_LEAVE) 여부 판정 및 퇴근 기록
     */
    @PostMapping("/checkout")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> checkOut() {
        return ResponseEntity.ok(attendanceService.checkOut(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID));
    }

    /**
     * 자리비움 시작
     * 응답: 변경된 실시간 상태(isAway: true)를 포함한 오늘 현황 반환
     */
    @PostMapping("/away/start")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> startAway() {
        attendanceService.startAway(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID);
        // 상태 변경 후 최신 상태를 다시 조회하여 반환
        return ResponseEntity.ok(attendanceService.getTodayAttendance(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID));
    }

    /**
     * 자리비움 종료
     * 응답: 변경된 실시간 상태(isAway: false)를 포함한 오늘 현황 반환
     */
    @PostMapping("/away/end")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> endAway() {
        attendanceService.endAway(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID);
        // 상태 변경 후 최신 상태를 다시 조회하여 반환
        return ResponseEntity.ok(attendanceService.getTodayAttendance(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID));
    }
}