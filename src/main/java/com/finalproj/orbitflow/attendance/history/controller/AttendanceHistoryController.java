package com.finalproj.orbitflow.attendance.history.controller;
import com.finalproj.orbitflow.attendance.history.dto.MonthlyHistoryResDto;
import com.finalproj.orbitflow.attendance.history.service.AttendanceHistoryService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 실무형 페이징 및 필터링이 적용된 근태 이력 컨트롤러
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
    public ResponseEntity<MonthlyHistoryResDto> getMonthlyHistory(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam int year, @RequestParam int month,
            @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 10) Pageable pageable) {


        return ResponseEntity.ok(attendanceHistoryService.getMonthlyHistoryData(
                user.getEmployeeId(), year, month, status, pageable));
    }
}