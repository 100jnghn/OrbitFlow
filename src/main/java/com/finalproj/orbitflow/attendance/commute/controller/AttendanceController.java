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

    // TODO: 실제 구현 시 SecurityContextHolder에서 가져오도록 수정 필요
    private final Long CURRENT_EMPLOYEE_ID = 1L;
    private final Long CURRENT_COMPANY_ID = 1L;

    /**
     * 오늘의 출퇴근 기록 조회
     */
    @GetMapping("/today")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> getTodayAttendance() {
        AttendanceDto.TodayAttendanceResponse response =
                attendanceService.getTodayAttendance(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID);
        return ResponseEntity.ok(response);
    }

    /**
     * 출근 처리
     */
    @PostMapping("/checkin")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> checkIn() {
        // [수정] 매개변수 순서: (companyId, employeeId) 서비스 정의와 일치시킴
        AttendanceDto.TodayAttendanceResponse response =
                attendanceService.checkIn(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 퇴근 처리
     */
    @PostMapping("/checkout")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> checkOut() {
        AttendanceDto.TodayAttendanceResponse response =
                attendanceService.checkOut(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID);
        return ResponseEntity.ok(response);
    }
}