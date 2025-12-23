package com.finalproj.orbitflow.leave.leaveGrant.controller;

import com.finalproj.orbitflow.leave.leaveGrant.repository.LeaveGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private final LeaveGrantRepository leaveGrantRepository;

    @PostMapping("/test-batch")
    public ResponseEntity<String> testGrant() {
        // 실제로는 전체 직원을 돌리거나, 특정 사원을 지정
        // 예: 사원번호 1번에 대해 2025년 연차 강제 부여
        // leaveGrantService.processAnnualLeaveGrant();
        return ResponseEntity.ok("연차 부여 배치 작업이 완료되었습니다.");
    }


}
