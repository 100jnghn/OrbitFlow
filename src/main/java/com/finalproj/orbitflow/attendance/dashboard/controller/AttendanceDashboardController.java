package com.finalproj.orbitflow.attendance.dashboard.controller;

import com.finalproj.orbitflow.attendance.dashboard.dto.AttendanceUpdateDto;
import com.finalproj.orbitflow.attendance.dashboard.dto.AdminSummaryResDto;
import com.finalproj.orbitflow.attendance.dashboard.service.AttendanceDashboardService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/attendance")
public class AttendanceDashboardController {

    private final AttendanceDashboardService attendanceDashboardService;

    @GetMapping("/list")
    public ResponseEntity<?> getAllEmployeeAttendance(
            @AuthenticationPrincipal SecurityUser admin,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(attendanceDashboardService.getCompanyAttendanceList(
                admin.getCompanyId(), startDate, endDate, status, keyword, pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryResDto> getTodaySummary(
            @AuthenticationPrincipal SecurityUser admin) {

        return ResponseEntity.ok(attendanceDashboardService.getTodaySummary(admin.getCompanyId()));
    }


    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateAttendance(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable(value = "id", required = false) Long id,
            @RequestBody AttendanceUpdateDto dto) {

        attendanceDashboardService.updateAttendanceRecord(id, admin.getCompanyId(), dto);
        return ResponseEntity.ok().build();
    }
}