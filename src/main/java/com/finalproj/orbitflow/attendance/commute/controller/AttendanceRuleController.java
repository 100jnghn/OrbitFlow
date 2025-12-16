package com.finalproj.orbitflow.attendance.commute.controller;


import com.finalproj.orbitflow.attendance.commute.dto.AttendanceRuleDto;
import com.finalproj.orbitflow.attendance.commute.dto.EmployeeAttRuleDto;
import com.finalproj.orbitflow.attendance.commute.service.AttendanceRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rules")
@RequiredArgsConstructor
public class AttendanceRuleController {

    private final AttendanceRuleService attendanceRuleService;

    // =======================================================
    // I. 회사 기본 규칙 (Default Rule) 관리 API
    // =======================================================

    /**
     * 1. 기본 규칙 조회 (GET /api/admin/rules/default)
     */
    @GetMapping("/default")
    public ResponseEntity<AttendanceRuleDto.AttendanceRuleResponse> getDefaultRule() {
        AttendanceRuleDto.AttendanceRuleResponse rule = attendanceRuleService.getDefaultRule();
        return ResponseEntity.ok(rule);
    }

    /**
     * 2. 기본 규칙 수정 (PUT /api/admin/rules/default)
     */
    @PutMapping("/default")
    public ResponseEntity<AttendanceRuleDto.AttendanceRuleResponse> updateDefaultRule(@RequestBody AttendanceRuleDto.AttendanceRuleUpdateRequest request) {
        AttendanceRuleDto.AttendanceRuleResponse updatedRule = attendanceRuleService.updateDefaultRule(request);
        return ResponseEntity.ok(updatedRule);
    }


    // =======================================================
    // II. 사원별 예외 규칙 (Exception Rule) 관리 API
    // =======================================================

    /**
     * 3. 예외 규칙 목록 조회 (GET /api/admin/rules/exception)
     */
    @GetMapping("/exception")
    public ResponseEntity<List<EmployeeAttRuleDto.EmployeeAttRuleResponse>> getExceptionRules() {
        List<EmployeeAttRuleDto.EmployeeAttRuleResponse> rules = attendanceRuleService.getExceptionRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * 4. 예외 규칙 상세 조회 (GET /api/admin/rules/exception/{ruleId})
     */
    @GetMapping("/exception/{ruleId}")
    public ResponseEntity<EmployeeAttRuleDto.EmployeeAttRuleResponse> getExceptionRuleDetail(@PathVariable Long ruleId) {
        EmployeeAttRuleDto.EmployeeAttRuleResponse rule = attendanceRuleService.getExceptionRuleDetail(ruleId);
        return ResponseEntity.ok(rule);
    }

    /**
     * 5. 예외 규칙 추가 (POST /api/admin/rules/exception)
     */
    @PostMapping("/exception")
    public ResponseEntity<EmployeeAttRuleDto.EmployeeAttRuleResponse> createExceptionRule(@RequestBody EmployeeAttRuleDto.EmployeeAttRuleCreateRequest request) {
        EmployeeAttRuleDto.EmployeeAttRuleResponse createdRule = attendanceRuleService.createExceptionRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
    }

    /**
     * 6. 예외 규칙 수정 (PUT /api/admin/rules/exception/{ruleId})
     */
    @PutMapping("/exception/{ruleId}")
    public ResponseEntity<EmployeeAttRuleDto.EmployeeAttRuleResponse> updateExceptionRule(@PathVariable Long ruleId, @RequestBody EmployeeAttRuleDto.EmployeeAttRuleUpdateRequest request) {
        EmployeeAttRuleDto.EmployeeAttRuleResponse updatedRule = attendanceRuleService.updateExceptionRule(ruleId, request);
        return ResponseEntity.ok(updatedRule);
    }

    /**
     * 7. 예외 규칙 삭제 (DELETE /api/admin/rules/exception/{ruleId})
     */
    @DeleteMapping("/exception/{ruleId}")
    public ResponseEntity<Void> deleteExceptionRule(@PathVariable Long ruleId) {
        attendanceRuleService.deleteExceptionRule(ruleId);
        return ResponseEntity.noContent().build();
    }
}