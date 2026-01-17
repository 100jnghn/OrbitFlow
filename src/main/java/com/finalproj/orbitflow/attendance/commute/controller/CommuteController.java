package com.finalproj.orbitflow.attendance.commute.controller;

import com.finalproj.orbitflow.attendance.commute.dto.ActiveRuleResDto;
import com.finalproj.orbitflow.attendance.commute.dto.EmployeeWorkStatusResDto;
import com.finalproj.orbitflow.attendance.commute.dto.TodayAttResDto;
import com.finalproj.orbitflow.attendance.commute.service.CommuteService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class CommuteController {

    private final CommuteService commuteService;

    @GetMapping("/active-rule")
    public ResponseEntity<?> getActiveRule(@AuthenticationPrincipal SecurityUser user) {
        ActiveRuleResDto rule = commuteService.getActiveRule(user.getCompanyId(), user.getEmployeeId());

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                "적용된 근태 규칙 정보를 성공적으로 가져왔습니다.",
                rule
        ));
    }


    @GetMapping("/today")
    public ResponseEntity<?> getTodayAttendance(@AuthenticationPrincipal SecurityUser user) {
        TodayAttResDto response = commuteService.getTodayAttendance(user.getCompanyId(), user.getEmployeeId());

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                "오늘의 근태 기록 조회 성공",
                response
        ));
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@AuthenticationPrincipal SecurityUser user) {
        TodayAttResDto result = commuteService.checkIn(user.getCompanyId(), user.getEmployeeId(),null);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto<>(
                HttpStatus.CREATED,
                "출근 처리가 완료되었습니다.",
                result
        ));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(@AuthenticationPrincipal SecurityUser user) {
        TodayAttResDto result = commuteService.checkOut(user.getCompanyId(), user.getEmployeeId());

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                "퇴근 처리가 완료되었습니다.",
                result
        ));
    }

    @PostMapping("/away/start")
    public ResponseEntity<?> startAway(@AuthenticationPrincipal SecurityUser user) {
        commuteService.startAway(user.getCompanyId(), user.getEmployeeId());
        TodayAttResDto result = commuteService.getTodayAttendance(user.getCompanyId(), user.getEmployeeId());

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                "자리비움 상태로 전환되었습니다.",
                result
        ));
    }

    @PostMapping("/away/end")
    public ResponseEntity<?> endAway(@AuthenticationPrincipal SecurityUser user) {
        commuteService.endAway(user.getCompanyId(), user.getEmployeeId());
        TodayAttResDto result = commuteService.getTodayAttendance(user.getCompanyId(), user.getEmployeeId());

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                "업무로 복귀하셨습니다. 자리비움이 해제되었습니다.",
                result
        ));
    }

    @GetMapping("/work-status/{employeeId}")
    public ResponseEntity<?> getEmployeeWorkStatus(
            @PathVariable Long employeeId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "사원 근무 상태 조회 성공",
                        new EmployeeWorkStatusResDto(
                                commuteService.getEmployeeWorkStatus(
                                        user.getCompanyId(),
                                        employeeId
                                )
                        )
                )
        );
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnFromOutsideOrTrip(@AuthenticationPrincipal SecurityUser user) {
        commuteService.returnFromOutsideOrTrip(
                user.getCompanyId(),
                user.getEmployeeId()
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "자리복귀 및 정상출근 처리 완료", null));
    }




}