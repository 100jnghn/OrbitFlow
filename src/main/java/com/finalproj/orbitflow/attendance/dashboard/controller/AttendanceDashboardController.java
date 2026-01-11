package com.finalproj.orbitflow.attendance.dashboard.controller;

import com.finalproj.orbitflow.attendance.dashboard.dto.AttendanceUpdateDto;
import com.finalproj.orbitflow.attendance.dashboard.dto.AdminSummaryResDto;
import com.finalproj.orbitflow.attendance.dashboard.service.AttendanceDashboardService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/attendance")
public class AttendanceDashboardController {

    private final AttendanceDashboardService attendanceDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ResponseDto<AdminSummaryResDto>> getTodaySummary(
            @AuthenticationPrincipal SecurityUser admin) {

        AdminSummaryResDto summary = attendanceDashboardService.getTodaySummary(admin.getCompanyId());

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                "금일 근태 요약 정보를 성공적으로 가져왔습니다.",
                summary
        ));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllEmployeeAttendance(
            @AuthenticationPrincipal SecurityUser admin,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {

        Object list = attendanceDashboardService.getCompanyAttendanceList(
                admin.getCompanyId(), startDate, endDate, status, keyword, pageable);

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                "전사 근태 목록을 성공적으로 불러왔습니다.",
                list
        ));
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateAttendance(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable(value = "id") Long id,
            @Valid @RequestBody AttendanceUpdateDto dto) {

        attendanceDashboardService.updateAttendanceRecord(id, admin.getCompanyId(), dto);

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                "근태 기록 정정이 성공적으로 완료되었습니다.",
                id
        ));
    }
}