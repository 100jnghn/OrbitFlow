package com.finalproj.orbitflow.leave.leaveHistory.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveHistoryResDto;
import com.finalproj.orbitflow.leave.leaveBalance.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveHistoryController
 * @since : 2025. 12. 29. 월요일
 */

@RestController
@RequestMapping("/api/leave/history")
@RequiredArgsConstructor
public class LeaveHistoryController {

    private final LeaveBalanceService leaveBalanceService;

    //연차 상세조회
    @GetMapping("/annual")
    public ResponseEntity<ResponseDto<List<LeaveHistoryResDto>>> getAnnualHistory(
            @AuthenticationPrincipal SecurityUser user) {

        List<LeaveHistoryResDto> history = leaveBalanceService.getAnnualLeaveHistory(
                user.getCompanyId(),
                user.getEmployeeId()
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "연차 상세 내역 조회 성공", history));
    }



    //기타 휴가 상세조회
    @GetMapping("/others")
    public ResponseEntity<ResponseDto<List<LeaveHistoryResDto>>> getOtherHistory(
            @AuthenticationPrincipal SecurityUser user) {

        List<LeaveHistoryResDto> history = leaveBalanceService.getOtherLeaveHistory(
                user.getCompanyId(),
                user.getEmployeeId()
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "기타 휴가 신청 내역 조회 성공", history));
    }
}