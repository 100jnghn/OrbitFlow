package com.finalproj.orbitflow.attendance.history.controller;

import com.finalproj.orbitflow.attendance.history.dto.MonthlyAttHistoryResDto;
import com.finalproj.orbitflow.attendance.history.service.AttendanceHistoryService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceHistoryController
 * @since : 2025. 12. 21. 일요일
 */

@RestController
@RequiredArgsConstructor
public class AttendanceHistoryController {

    private final AttendanceHistoryService attendanceHistoryService;

    /**
     * 월별 근태 현황 조회
     * GET /api/attendance/history/monthly?year=2025&month=12
     */
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAttHistoryResDto> getMonthlyHistory(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam int year,
            @RequestParam int month) {

        MonthlyAttHistoryResDto response = attendanceHistoryService.getMonthlyHistory(
                user.getEmployeeId(),
                year,
                month
        );
        return ResponseEntity.ok(response);
    }
}
