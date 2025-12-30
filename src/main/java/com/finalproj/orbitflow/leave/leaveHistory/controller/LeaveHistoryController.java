package com.finalproj.orbitflow.leave.leaveHistory.controller;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveHistoryResDto;
import com.finalproj.orbitflow.leave.leaveBalance.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    //연차 상세조회 (페이지네이션 및 필터링 지원)
    @GetMapping("/annual")
    public ResponseEntity<ResponseDto<Page<LeaveHistoryResDto>>> getAnnualHistory(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "startDate") Pageable pageable) {

        Page<LeaveHistoryResDto> history;
        
        // 필터가 하나라도 있으면 필터링 메서드 사용
        if (typeName != null || status != null || startDate != null || endDate != null) {
            history = leaveBalanceService.getAnnualLeaveHistory(
                    user.getCompanyId(),
                    user.getEmployeeId(),
                    typeName,
                    status,
                    startDate,
                    endDate,
                    pageable
            );
        } else {
            // 필터가 없으면 기본 메서드 사용
            history = leaveBalanceService.getAnnualLeaveHistory(
                    user.getCompanyId(),
                    user.getEmployeeId(),
                    pageable
            );
        }

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
    
    // 모든 휴가 이력 조회 (차감/비차감 구분 없이 모든 휴가 종류 포함)
    @GetMapping("/all")
    public ResponseEntity<ResponseDto<Page<LeaveHistoryResDto>>> getAllHistory(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "startDate") Pageable pageable) {

        Page<LeaveHistoryResDto> history = leaveBalanceService.getAllLeaveHistory(
                user.getCompanyId(),
                user.getEmployeeId(),
                typeName,
                status,
                startDate,
                endDate,
                pageable
        );

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "휴가 신청 현황 조회 성공", history));
    }
}