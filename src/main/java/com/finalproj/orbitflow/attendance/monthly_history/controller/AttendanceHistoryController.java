package com.finalproj.orbitflow.attendance.monthly_history.controller;
import com.finalproj.orbitflow.attendance.monthly_history.dto.MonthlyHistoryResDto;
import com.finalproj.orbitflow.attendance.monthly_history.service.AttendanceHistoryService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam int year, @RequestParam int month,
            @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 10) Pageable pageable) {

        MonthlyHistoryResDto data = attendanceHistoryService.getMonthlyHistoryData(
                user.getEmployeeId(), year, month, status, pageable);

        ResponseDto<MonthlyHistoryResDto> response = new ResponseDto<>(
                HttpStatus.OK,
                year + "년 " + month + "월 근태 내역 조회가 성공적으로 완료되었습니다.",
                data
        );

        return ResponseEntity.ok(response);


    }
}