package com.finalproj.orbitflow.leave.leaveType.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.leave.leaveType.dto.LeaveTypeResDto;
import com.finalproj.orbitflow.leave.leaveType.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    /**
     * 모든 휴가 유형 조회
     */
    @GetMapping("/types")
    public ResponseEntity<ResponseDto<List<LeaveTypeResDto>>> getAllLeaveTypes() {
        List<LeaveTypeResDto> leaveTypes = leaveTypeService.getAllLeaveTypes();
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "휴가 유형 조회 성공", leaveTypes));
    }
}
