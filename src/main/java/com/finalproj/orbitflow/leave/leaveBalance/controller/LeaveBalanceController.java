package com.finalproj.orbitflow.leave.leaveBalance.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveBalanceResDto;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveHistoryResDto;
import com.finalproj.orbitflow.leave.leaveBalance.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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


    @GetMapping("/history/annual")
    public ResponseEntity<ResponseDto<List<LeaveHistoryResDto>>> getAnnualHistory(
            @AuthenticationPrincipal SecurityUser user) {

        List<LeaveHistoryResDto> history = leaveBalanceService.getAnnualLeaveHistory(
                user.getCompanyId(),
                user.getEmployeeId()
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "연차 상세 내역 조회 성공", history));
    }


    @GetMapping("/history/others")
    public ResponseEntity<ResponseDto<List<LeaveHistoryResDto>>> getOtherHistory(
            @AuthenticationPrincipal SecurityUser user) {

        List<LeaveHistoryResDto> history = leaveBalanceService.getOtherLeaveHistory(
                user.getCompanyId(),
                user.getEmployeeId()
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "기타 휴가 신청 내역 조회 성공", history));
    }
}