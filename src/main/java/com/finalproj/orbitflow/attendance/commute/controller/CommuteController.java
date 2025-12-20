package com.finalproj.orbitflow.attendance.commute.controller;

import com.finalproj.orbitflow.attendance.commute.dto.ActiveRuleResDto;
import com.finalproj.orbitflow.attendance.commute.dto.TodayAttResDto;
import com.finalproj.orbitflow.attendance.commute.service.CommuteService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class CommuteController {

    private final CommuteService commuteService;


    @GetMapping("/active-rule")
    public ResponseEntity<?> getActiveRule(@AuthenticationPrincipal SecurityUser user) {
        if (user == null) return ResponseEntity.status(401).build();

        // 서비스 호출 시 반드시 user.getEmployeeId()를 넘겨야 사원별 예외를 찾을 수 있습니다.
        ActiveRuleResDto rule = commuteService.getActiveRule(user.getCompanyId(), user.getEmployeeId());
        return ResponseEntity.ok(rule);
    }

    /**
     * 오늘 출근 현황 조회
     * (출근 전이면 "근무예정", 출근 후면 "정상출근/지각" 및 "자리비움 여부" 반환)
     */
    @GetMapping("/today")
    public ResponseEntity<TodayAttResDto> getTodayAttendance(
            @AuthenticationPrincipal SecurityUser user) {
        TodayAttResDto response= commuteService.getTodayAttendance(user.getCompanyId(), user.getEmployeeId());
        return ResponseEntity.ok(response);
    }

    /**
     * 출근 처리
     * 결과: 정상출근(ON_TIME) 또는 지각(LATE) 판정
     */
    @PostMapping("/checkin")
    public ResponseEntity<TodayAttResDto> checkIn(
            @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commuteService.checkIn(user.getCompanyId(), user.getEmployeeId()));
    }

    /**
     * 퇴근 처리
     * 결과: 조퇴(EARLY_LEAVE) 여부 판정 및 퇴근 기록
     */
    @PostMapping("/checkout")
    public ResponseEntity<TodayAttResDto> checkOut(
            @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.ok(commuteService.checkOut(user.getCompanyId(), user.getEmployeeId()));
    }

    /**
     * 자리비움 시작
     * 응답: 변경된 실시간 상태(isAway: true)를 포함한 오늘 현황 반환
     */
    @PostMapping("/away/start")
    public ResponseEntity<TodayAttResDto> startAway(
            @AuthenticationPrincipal SecurityUser user) {
        commuteService.startAway(user.getCompanyId(), user.getEmployeeId());
        // 상태 변경 후 최신 상태를 다시 조회하여 반환
        return ResponseEntity.ok(commuteService.getTodayAttendance(user.getCompanyId(), user.getEmployeeId()));
    }

    /**
     * 자리비움 종료
     * 응답: 변경된 실시간 상태(isAway: false)를 포함한 오늘 현황 반환
     */
    @PostMapping("/away/end")
    public ResponseEntity<TodayAttResDto> endAway(
            @AuthenticationPrincipal SecurityUser user) {
        commuteService.endAway(user.getCompanyId(), user.getEmployeeId());
        // 상태 변경 후 최신 상태를 다시 조회하여 반환
        return ResponseEntity.ok(commuteService.getTodayAttendance(user.getCompanyId(), user.getEmployeeId()));
    }
}