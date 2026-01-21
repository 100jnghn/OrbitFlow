package com.finalproj.orbitflow.attendance.monthlyhistory.controller;
import com.finalproj.orbitflow.attendance.monthlyhistory.dto.MonthlyHistoryResDto;
import com.finalproj.orbitflow.attendance.monthlyhistory.service.AttendanceHistoryService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * * @author : rlagkdus
 * @filename : AttendanceHistoryController
 * @since : 2025. 12. 21. 일요일
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance/history")
public class AttendanceHistoryController {

    private final AttendanceHistoryService attendanceHistoryService;

    @GetMapping("/monthly")
    public ResponseEntity<ResponseDto<MonthlyHistoryResDto>> getMonthlyHistory(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 31) Pageable pageable) {


        MonthlyHistoryResDto data = attendanceHistoryService.getMonthlyHistoryData(
                user.getEmployeeId(), year, month, startDate, endDate, status, pageable);

        String message = String.format("[%s] 근태 내역 조회가 완료되었습니다.", data.getSearchPeriod());

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, message, data));
    }
}