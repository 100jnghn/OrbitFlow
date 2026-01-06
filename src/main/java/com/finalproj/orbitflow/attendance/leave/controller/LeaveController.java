package com.finalproj.orbitflow.attendance.leave.controller;

import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import com.finalproj.orbitflow.attendance.leave.dto.*;
import com.finalproj.orbitflow.attendance.leave.service.LeaveService;
import com.finalproj.orbitflow.attendance.leave.service.LeaveTypeService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final LeaveTypeService leaveTypeService;


    @PostMapping("/admin/leave/batch-grant")
    public ResponseEntity<?> manualBatchGrant(
            @RequestParam Long companyId,
            @RequestParam Integer year) {

        leaveService.batchGrantAnnualLeave(companyId, year);

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                year + "년도 회계년도 기준 연차 일괄 부여가 완료되었습니다.",
                null));
    }


    @PostMapping("/admin/leave/expire-process")
    public ResponseEntity<?> manualExpireProcess() {
        leaveService.expireOutdatedLeaves();

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                "연차 소멸 프로세스가 성공적으로 실행되었습니다.",
                null));
    }


    @GetMapping("/leave/summary")
    public ResponseEntity<?> getMySummary(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer year) {

        LeaveBalanceResDto result = leaveService.getMySummary(user.getCompanyId(), user.getEmployeeId(), year);
        String message = (year == null ? "올해" : year + "년도") + " 실제 승인 내역이 반영된 연차 현황입니다.";

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, message, result));
    }


    @GetMapping("/leave/usage")
    public ResponseEntity<?> getMyLeaveUsage(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "startDate") Pageable pageable) {

        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        // 디버깅 로그
        System.out.println("필터 파라미터 - year: " + targetYear + ", typeName: " + typeName +
                ", status: " + status + ", startDate: " + startDate + ", endDate: " + endDate);

        // isCountable = true 인 내역만 조회하도록 서비스 호출 (필터 파라미터 추가)
        Page<LeaveHistoryResDto> usageHistory = leaveService.getLeaveUsageHistory(
                user.getCompanyId(), user.getEmployeeId(), targetYear, typeName, status, startDate, endDate, pageable);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, targetYear + "년도 연차 차감 내역입니다.", usageHistory));
    }


    @GetMapping("/leave/history")
    public ResponseEntity<?> getMyHistory(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "startDate") Pageable pageable) {

        Page<LeaveHistoryResDto> history = leaveService.getAllLeaveHistory(
                user.getCompanyId(), user.getEmployeeId(), typeName, status, startDate, endDate, pageable);

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "휴가 신청 내역 조회가 완료되었습니다.", history));
    }


    @GetMapping("/leave/types")
    public ResponseEntity<?> getLeaveTypes(@RequestParam(required = false) Boolean isCountable) {
        List<LeaveTypeResDto> types = (isCountable != null && isCountable) ?
                leaveTypeService.getCountableLeaveTypes() : leaveTypeService.getAllLeaveTypes();

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "신청 가능한 휴가 유형 목록입니다.", types));
    }

    @GetMapping("/leave/remaining")
    public ResponseEntity<?> getLeaveRemaining() {
        LeaveRemainingResDto result = leaveService.getLeaveRemaining(SecurityUtils.getEmployeeId());

        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "잔여 연차 조회 성공", result));
    }

    @PostMapping("/leave/validate")
    public ResponseEntity<?> validateLeave(
            @RequestBody LeaveValidationReqDto reqDto
    ) {
        LeaveValidationResDto result = leaveService.validateLeave(SecurityUtils.getEmployeeId(), reqDto);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "연차 사용 검증 결과 반환", result));
    }
}