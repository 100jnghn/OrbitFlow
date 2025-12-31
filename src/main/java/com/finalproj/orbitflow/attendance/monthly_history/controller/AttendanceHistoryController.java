package com.finalproj.orbitflow.attendance.monthly_history.controller;
import com.finalproj.orbitflow.attendance.monthly_history.dto.MonthlyHistoryResDto;
import com.finalproj.orbitflow.attendance.monthly_history.service.AttendanceHistoryService;
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
            // 연, 월은 선택사항으로 변경 (없으면 오늘 기준)
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            // 자유 기간 설정을 위한 파라미터 추가
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 31) Pageable pageable) { // 월별 조회이므로 기본 사이즈를 31로 권장

        // 기간 유효성 검사: 시작일이 종료일보다 늦은 경우
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDto<>(HttpStatus.BAD_REQUEST, "시작일이 종료일보다 늦을 수 없습니다.", null));
        }

        // 서비스에서 기간 우선순위 로직 처리
        MonthlyHistoryResDto data = attendanceHistoryService.getMonthlyHistoryData(
                user.getEmployeeId(), year, month, startDate, endDate, status, pageable);

        // 메시지 동적 처리
        String message = (startDate != null && endDate != null)
                ? startDate + " ~ " + endDate + " 근태 내역 조회가 완료되었습니다."
                : (year != null ? year : LocalDate.now().getYear()) + "년 "
                + (month != null ? month : LocalDate.now().getMonthValue()) + "월 근태 내역 조회가 완료되었습니다.";

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, message, data));
    }
}