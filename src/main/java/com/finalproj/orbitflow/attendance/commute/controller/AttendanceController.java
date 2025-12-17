package com.finalproj.orbitflow.attendance.commute.controller;

import com.finalproj.orbitflow.attendance.commute.dto.AttendanceDto;
import com.finalproj.orbitflow.attendance.commute.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 출퇴근 관리 API 컨트롤러
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // TODO: 실제 구현 시 Spring Security 등을 통해 현재 로그인한 사원 ID와 회사 ID를 가져와야 함
    private final Long CURRENT_EMPLOYEE_ID = 1L;
    private final Long CURRENT_COMPANY_ID = 1L;

    /**
     * 오늘의 출퇴근 기록 조회 (GET /api/attendance/today)
     */
//    @GetMapping("/today")
//    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> getTodayAttendance() {
//        AttendanceDto.TodayAttendanceResponse response = attendanceService.getTodayAttendance(CURRENT_EMPLOYEE_ID);
//        return ResponseEntity.ok(response);
//    }

    /**
     * 출근 처리 (POST /api/attendance/checkin)
     */
    @PostMapping("/checkin")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> checkIn() {
        AttendanceDto.TodayAttendanceResponse response = attendanceService.checkIn(CURRENT_EMPLOYEE_ID, CURRENT_COMPANY_ID);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 퇴근 처리 (POST /api/attendance/checkout)
     */
//    @PostMapping("/checkout")
//    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> checkOut() {
//        AttendanceDto.TodayAttendanceResponse response = attendanceService.checkOut(CURRENT_EMPLOYEE_ID);
//        return ResponseEntity.ok(response);
//    }
}

