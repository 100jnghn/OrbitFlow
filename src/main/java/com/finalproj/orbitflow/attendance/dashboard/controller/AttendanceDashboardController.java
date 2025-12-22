package com.finalproj.orbitflow.attendance.dashboard.controller;

import com.finalproj.orbitflow.attendance.dashboard.dto.AttendanceUpdateDto;
import com.finalproj.orbitflow.attendance.dashboard.service.AttendanceDashboardService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceDashboardViewController
 * @since : 2025. 12. 19. 금요일
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/attendance")
public class AttendanceDashboardController {

    private final AttendanceDashboardService attendanceDashboardService;

    /**
     * 전사 직원 근태 현황 조회 (페이징 및 필터)
     */
    @GetMapping("/list")
    public ResponseEntity<?> getAllEmployeeAttendance(
            @AuthenticationPrincipal SecurityUser admin,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(attendanceDashboardService.getCompanyAttendanceList(
                admin.getCompanyId(), startDate, endDate, status, pageable));
    }

    /**
     * 직원 근태 기록 수정
     */
    @PatchMapping("/update/{attendanceId}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long attendanceId,
            @RequestBody AttendanceUpdateDto updateDto) {

        attendanceDashboardService.updateAttendanceRecord(attendanceId, updateDto);
        return ResponseEntity.ok().build();
    }
}