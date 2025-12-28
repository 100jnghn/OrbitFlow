package com.finalproj.orbitflow.leave.leaveType.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.leave.leaveType.dto.LeaveTypeResDto;
import com.finalproj.orbitflow.leave.leaveType.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveTypeController
 * @since : 2025. 12. 24. 수요일
 */

@RestController
@RequestMapping("/api/leave-types")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping("/all")
    public ResponseEntity<ResponseDto> getAllLeaveTypes() {

        List<LeaveTypeResDto> result = leaveTypeService.getAllSeaveTypes();

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "휴가 유형 조회 성공", result));
    }
}
