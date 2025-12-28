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
     * [관리자] 전체 사원 연차 일괄 부여
     */
    @PostMapping("/grant/run")
    public ResponseEntity<String> runAnnualLeaveGrant() {
        leaveGrantService.grantAnnualLeave();
        return ResponseEntity.ok("연차 부여 공정이 완료되었습니다.");
    }


}