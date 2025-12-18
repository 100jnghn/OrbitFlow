package com.finalproj.orbitflow.attendance.attendanceRule.controller;

import com.finalproj.orbitflow.attendance.attendanceRule.dto.AttRuleResDto;
import com.finalproj.orbitflow.attendance.attendanceRule.dto.AttRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.employeeAttRule.dto.EmpAttRuleCreateReqDto;
import com.finalproj.orbitflow.attendance.employeeAttRule.dto.EmpAttRuleResDto;
import com.finalproj.orbitflow.attendance.employeeAttRule.dto.EmpAttRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.attendanceRule.service.AttendanceRuleService;
import com.finalproj.orbitflow.global.security.SecurityUser;
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
    // I. 회사 기본 규칙 (Default Rule) 관리 API
    // =======================================================

    /**
     * 1. 기본 규칙 조회 (GET /api/admin/rules/default)
     */
    @GetMapping("/default")
    public ResponseEntity<AttRuleResDto> getDefaultRule(
            @AuthenticationPrincipal SecurityUser admin) { // 관리자 세션 정보 주입

        AttRuleResDto rule = attendanceRuleService.getDefaultRule(admin.getCompanyId());
        return ResponseEntity.ok(rule);
    }

    /**
     * 2. 기본 규칙 수정 (PUT /api/admin/rules/default)
     */
    @PutMapping("/default")
    public ResponseEntity<AttRuleResDto> updateDefaultRule(
            @AuthenticationPrincipal SecurityUser admin,
            @RequestBody AttRuleUpdateReqDto request) {

        AttRuleResDto updatedRule = attendanceRuleService.updateDefaultRule(admin.getCompanyId(), request);
        return ResponseEntity.ok(updatedRule);
    }


    // =======================================================
    // II. 사원별 예외 규칙 (Exception Rule) 관리 API
    // =======================================================

    /**
     * 3. 예외 규칙 목록 조회 (GET /api/admin/rules/exception)
     */
    @GetMapping("/exception")
    public ResponseEntity<List<EmpAttRuleResDto>> getExceptionRules(
            @AuthenticationPrincipal SecurityUser admin) { // 관리자 세션 정보 주입

        // 관리자 소속 회사의 예외 규칙만 조회
        List<EmpAttRuleResDto> rules = attendanceRuleService.getExceptionRules(admin.getCompanyId());
        return ResponseEntity.ok(rules);
    }

    /**
     * 4. 예외 규칙 상세 조회 (GET /api/admin/rules/exception/{ruleId})
     */
    @GetMapping("/exception/{ruleId}")
    public ResponseEntity<EmpAttRuleResDto> getExceptionRuleDetail(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable Long ruleId) {

        // 상세 조회 시에도 해당 규칙이 관리자의 회사 소속인지 확인
        EmpAttRuleResDto rule = attendanceRuleService.getExceptionRuleDetail(admin.getCompanyId(), ruleId);
        return ResponseEntity.ok(rule);
    }

    /**
     * 5. 예외 규칙 추가 (POST /api/admin/rules/exception)
     */
    /**
     * 5. 예외 규칙 추가 (POST /api/admin/rules/exception)
     */
    @PostMapping("/exception")
    public ResponseEntity<EmpAttRuleResDto> createExceptionRule(
            @AuthenticationPrincipal SecurityUser admin, // 세션에서 관리자 정보 주입
            @RequestBody EmpAttRuleCreateReqDto request) {

        // 서비스 호출 시 admin 객체를 함께 전달
        EmpAttRuleResDto createdRule = attendanceRuleService.createExceptionRule(admin, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
    }

    /**
     * 6. 예외 규칙 수정 (PUT /api/admin/rules/exception/{ruleId})
     */
    @PutMapping("/exception/{ruleId}")
    public ResponseEntity<EmpAttRuleResDto> updateExceptionRule(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable Long ruleId,
            @RequestBody EmpAttRuleUpdateReqDto request) {

        // 서비스 호출 시 관리자의 정보(admin)를 함께 넘겨주어 본인 회사의 데이터인지 확인하게 합니다.
        EmpAttRuleResDto updatedRule = attendanceRuleService.updateExceptionRule(admin, ruleId, request);

        return ResponseEntity.ok(updatedRule);
    }

    /**
     * 7. 예외 규칙 삭제 (DELETE /api/admin/rules/exception/{ruleId})
     */
    @DeleteMapping("/exception/{ruleId}")
    public ResponseEntity<String> deleteExceptionRule(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable Long ruleId) {

        // 서비스에서 삭제 로직 수행
        attendanceRuleService.deleteExceptionRule(admin, ruleId);

        // 포스트맨 Body에 띄울 메시지 반환
        return ResponseEntity.ok("성공적으로 삭제되었습니다. (규칙 ID: " + ruleId + ")");
    }
}