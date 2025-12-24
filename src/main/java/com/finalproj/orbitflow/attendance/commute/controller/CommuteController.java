package com.finalproj.orbitflow.attendance.commute.controller;

import com.finalproj.orbitflow.attendance.commute.dto.ActiveRuleResDto;
import com.finalproj.orbitflow.attendance.commute.dto.TodayAttResDto;
import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.service.CommuteService;
import com.finalproj.orbitflow.global.common.ResponseDto;
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


    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@AuthenticationPrincipal SecurityUser user) {

        TodayAttResDto result = commuteService.checkIn(user.getCompanyId(), user.getEmployeeId());

        ResponseDto<TodayAttResDto> response = new ResponseDto<>(
                HttpStatus.CREATED,
                "출근 처리가 완료되었습니다.",
                result
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(@AuthenticationPrincipal SecurityUser user) {
        TodayAttResDto result = commuteService.checkOut(user.getCompanyId(), user.getEmployeeId());

        ResponseDto<TodayAttResDto> response = new ResponseDto<>(
                HttpStatus.OK,
                "퇴근 처리가 완료되었습니다.",
                result
        );
        return ResponseEntity.ok(response);
    }


    @PostMapping("/away/start")
    public ResponseEntity<TodayAttResDto> startAway(
            @AuthenticationPrincipal SecurityUser user) {
        commuteService.startAway(user.getCompanyId(), user.getEmployeeId());
        return ResponseEntity.ok(commuteService.getTodayAttendance(user.getCompanyId(), user.getEmployeeId()));
    }


    @PostMapping("/away/end")
    public ResponseEntity<TodayAttResDto> endAway(
            @AuthenticationPrincipal SecurityUser user) {
        commuteService.endAway(user.getCompanyId(), user.getEmployeeId());
        return ResponseEntity.ok(commuteService.getTodayAttendance(user.getCompanyId(), user.getEmployeeId()));
    }
}