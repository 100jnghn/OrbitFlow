package com.finalproj.orbitflow.attendance.leave.leaveBalance.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.attendance.leave.leaveBalance.dto.LeaveBalanceResDto;
import com.finalproj.orbitflow.attendance.leave.leaveBalance.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 내 연차 및 휴가 이력 조회 컨트롤러
 *
 * @author : rlagkdus
 * @filename : LeaveBalanceController
 * @since : 2025. 12. 24. 수요일
 */
@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    // 부여연차/사용연차/잔여연차 조회
    @GetMapping("/my")
    public ResponseEntity<ResponseDto<LeaveBalanceResDto>> getMyBalance(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer year) {

        LeaveBalanceResDto balance = leaveBalanceService.getMySummary(
                user.getCompanyId(),
                user.getEmployeeId(),
                year
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "연차 요약 조회 성공", balance));
    }



}