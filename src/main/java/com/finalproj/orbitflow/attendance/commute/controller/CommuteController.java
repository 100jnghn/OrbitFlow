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
        // 1. 인증 객체 널 체크 (NPE 방지 및 401 에러 처리)
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 현재 날짜 기준 적용 시간 조회
        // CommuteService 내부에 사원별 예외 규칙을 먼저 찾고, 없으면 기본 규칙을 찾는 로직이 구현되어 있어야 합니다.
        LocalDate today = LocalDate.now();
        LocalTime startTime = commuteService.getApplicableStartTime(user.getCompanyId(), user.getEmployeeId(), today);
        LocalTime endTime = commuteService.getApplicableEndTime(user.getCompanyId(), user.getEmployeeId(), today);

        // 3. 결과 반환
        return ResponseEntity.ok(new ActiveRuleResDto(startTime, endTime));
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