package com.finalproj.orbitflow.leave.leaveGrant.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveBalanceResDto;
import com.finalproj.orbitflow.leave.leaveBalance.dto.LeaveHistoryResDto;
import com.finalproj.orbitflow.leave.leaveBalance.service.LeaveBalanceService;
import com.finalproj.orbitflow.leave.leaveGrant.service.LeaveGrantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveGrantController
 * @since : 2025. 12. 24. 수요일
 */
@RestController
@RequestMapping("/api/leave/admin")
@RequiredArgsConstructor
public class LeaveGrantController {

    private final LeaveGrantService leaveGrantService;

    /**
     * 1. 회계년도 기준 연차 일괄 부여 테스트
     */
    @PostMapping("/batch-grant")
    public ResponseEntity<String> manualBatchGrant(
            @RequestParam Long companyId,
            @RequestParam Integer year) {
        leaveGrantService.batchGrantAnnualLeave(companyId, year);
        return ResponseEntity.ok(year + "년도 연차 일괄 부여가 완료되었습니다.");
    }

    /**
     * 2. 연차 소멸 프로세스 강제 실행 테스트
     */
    @PostMapping("/expire-process")
    public ResponseEntity<String> manualExpireProcess() {
        leaveGrantService.expireOutdatedLeaves();
        return ResponseEntity.ok("연차 소멸 프로세스가 강제 실행되었습니다.");
    }


}