package com.finalproj.orbitflow.attendance.default_rule.controller;

import com.finalproj.orbitflow.attendance.default_rule.dto.AttRuleResDto;
import com.finalproj.orbitflow.attendance.default_rule.dto.AttRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.default_rule.dto.EmployeeSearchDto;
import com.finalproj.orbitflow.attendance.exception_rule.dto.EmpAttRuleCreateReqDto;
import com.finalproj.orbitflow.attendance.exception_rule.dto.EmpAttRuleResDto;
import com.finalproj.orbitflow.attendance.exception_rule.dto.EmpAttRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.default_rule.service.AttendanceRuleService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rules")
@RequiredArgsConstructor
public class AttendanceRuleController {

    private final AttendanceRuleService attendanceRuleService;

    // =======================================================
    // I. 회사 기본 규칙 (Default Rule)
    // =======================================================

    @GetMapping("/default")
    public ResponseEntity<AttRuleResDto> getDefaultRule(
            @AuthenticationPrincipal SecurityUser admin) {
        return ResponseEntity.ok(attendanceRuleService.getDefaultRule(admin.getCompanyId()));
    }

    @PutMapping("/default")
    public ResponseEntity<AttRuleResDto> updateDefaultRule(
            @AuthenticationPrincipal SecurityUser admin,
            @RequestBody AttRuleUpdateReqDto request) {
        return ResponseEntity.ok(attendanceRuleService.updateDefaultRule(admin.getCompanyId(), request));
    }


    // =======================================================
    // II. 사원별 예외 규칙 (Exception Rule) 관리 API
    // =======================================================

    @GetMapping("/exception")
    public ResponseEntity<List<EmpAttRuleResDto>> getExceptionRules(
            @AuthenticationPrincipal SecurityUser admin) {
        return ResponseEntity.ok(attendanceRuleService.getExceptionRules(admin.getCompanyId()));
    }

    @GetMapping("/exception/{ruleId}")
    public ResponseEntity<EmpAttRuleResDto> getExceptionRuleDetail(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable Long ruleId) {
        return ResponseEntity.ok(attendanceRuleService.getExceptionRuleDetail(admin.getCompanyId(), ruleId));
    }

    @PostMapping("/exception")
    public ResponseEntity<EmpAttRuleResDto> createExceptionRule(
            @AuthenticationPrincipal SecurityUser admin,
            @RequestBody EmpAttRuleCreateReqDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceRuleService.createExceptionRule(admin, request));
    }

    @PutMapping("/exception/{ruleId}")
    public ResponseEntity<EmpAttRuleResDto> updateExceptionRule(
            @PathVariable Long ruleId,
            @RequestBody EmpAttRuleUpdateReqDto request) {
        return ResponseEntity.ok(attendanceRuleService.updateExceptionRule(SecurityUtils.getCompanyId(), ruleId, request));
    }

    @DeleteMapping("/exception/{ruleId}")
    public ResponseEntity<Void> deleteExceptionRule(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable Long ruleId) {
        attendanceRuleService.deleteExceptionRule(admin, ruleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employees/search")
    public ResponseEntity<List<EmployeeSearchDto>> searchEmployees(
            @AuthenticationPrincipal SecurityUser admin,
            @RequestParam String keyword) {
        return ResponseEntity.ok(attendanceRuleService.searchEmployees(admin.getCompanyId(), keyword));
    }
}