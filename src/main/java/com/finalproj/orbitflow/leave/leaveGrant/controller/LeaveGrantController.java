package com.finalproj.orbitflow.leave.leaveGrant.controller;

import com.finalproj.orbitflow.leave.leaveGrant.service.LeaveGrantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveGrantController
 * @since : 2025. 12. 24. 수요일
 */

@RestController
@RequestMapping("/api/admin/leave/grant")
@RequiredArgsConstructor
public class LeaveGrantController {

    private final LeaveGrantService leaveGrantService;


    //전체 사원 대상 연차 일괄 부여 실행 (수동 트리거)
    @PostMapping("/run")
    public ResponseEntity<String> runAnnualLeaveGrant() {
        leaveGrantService.grantAnnualLeave(); //
        return ResponseEntity.ok("회계연도 기준 연차 부여 공정 및 잔합 업데이트가 완료되었습니다.");
    }


//    //특정 사원의 연차 부여 이력 조회
//    @GetMapping("/history/{employeeId}")
//    public ResponseEntity<?> getEmployeeGrantHistory(@PathVariable Long employeeId) {
//        // service에서 이력 조회 메서드를 구현하여 연결 가능합니다.
//        return ResponseEntity.ok(leaveGrantService.getHistoryByEmployee(employeeId));
//    }





}
