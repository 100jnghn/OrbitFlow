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

    private final Long CURRENT_EMPLOYEE_ID = 1L;
    private final Long CURRENT_COMPANY_ID = 1L;

    @GetMapping("/today")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> getTodayAttendance() {
        return ResponseEntity.ok(attendanceService.getTodayAttendance(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID));
    }

    @PostMapping("/checkin")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> checkIn() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.checkIn(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID));
    }

    @PostMapping("/checkout")
    public ResponseEntity<AttendanceDto.TodayAttendanceResponse> checkOut() {
        return ResponseEntity.ok(attendanceService.checkOut(CURRENT_COMPANY_ID, CURRENT_EMPLOYEE_ID));
    }
}