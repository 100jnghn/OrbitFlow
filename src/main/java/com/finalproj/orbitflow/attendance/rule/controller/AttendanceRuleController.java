package com.finalproj.orbitflow.attendance.rule.controller;

import com.finalproj.orbitflow.attendance.rule.dto.response.DefaultRuleResDto;
import com.finalproj.orbitflow.attendance.rule.dto.request.DefaultRuleUpdateReqDto;
import com.finalproj.orbitflow.attendance.rule.entity.AttendanceRule;
import com.finalproj.orbitflow.attendance.rule.service.AttendanceRuleService;
import com.finalproj.orbitflow.attendance.rule.dto.request.EmpAttRuleCreateReqDto;
import com.finalproj.orbitflow.attendance.rule.dto.response.EmployeeRuleResDto;
import com.finalproj.orbitflow.attendance.rule.dto.request.EmpAttRuleUpdateReqDto;
import com.finalproj.orbitflow.global.common.ResponseDto; // 공통 응답 DTO 임포트
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.service.CompanyService;
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
 * @filename : AttendanceRuleController
 * @since : 2025. 12. 22. 월요일
 */


@RestController
@RequestMapping("/api/admin/rules")
@RequiredArgsConstructor
public class AttendanceRuleController {

    private final AttendanceRuleService attendanceRuleService;
    private final CompanyService companyService;

    @GetMapping("/default")
    public ResponseEntity<?> getDefaultRule(@AuthenticationPrincipal SecurityUser admin) {
        DefaultRuleResDto result = attendanceRuleService.getDefaultRule(admin.getCompanyId());
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "기본 규칙 조회 성공", result));
    }

    @PutMapping("/default")
    public ResponseEntity<?> updateDefaultRule(
            @AuthenticationPrincipal SecurityUser admin,
            @RequestBody DefaultRuleUpdateReqDto request) {
        DefaultRuleResDto result = attendanceRuleService.updateDefaultRule(admin.getCompanyId(), request);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "회사 기본 규칙이 성공적으로 수정되었습니다.", result));
    }

    @GetMapping("/exception")
    public ResponseEntity<?> getExceptionRules(@AuthenticationPrincipal SecurityUser admin) {
        List<EmployeeRuleResDto> result = attendanceRuleService.getExceptionRules(admin.getCompanyId());
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "예외 규칙 목록 조회 성공", result));
    }

    @GetMapping("/exception/{ruleId}")
    public ResponseEntity<?> getExceptionRuleDetail(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable Long ruleId) {
        EmployeeRuleResDto result = attendanceRuleService.getExceptionRuleDetail(admin.getCompanyId(), ruleId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "예외 규칙 상세 조회 성공", result));
    }

    @PostMapping("/exception")
    public ResponseEntity<?> createExceptionRule(
            @AuthenticationPrincipal SecurityUser admin,
            @RequestBody EmpAttRuleCreateReqDto request) {
        EmployeeRuleResDto result = attendanceRuleService.createExceptionRule(admin.getCompanyId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto<>(HttpStatus.CREATED, "사원별 예외 규칙이 성공적으로 등록되었습니다.", result));
    }

    @PutMapping("/exception/{ruleId}")
    public ResponseEntity<?> updateExceptionRule(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable Long ruleId,
            @RequestBody EmpAttRuleUpdateReqDto request) {

        EmployeeRuleResDto result = attendanceRuleService.updateExceptionRule(admin.getCompanyId(), ruleId, request);

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.OK,
                "사원별 예외 규칙이 성공적으로 수정되었습니다.",
                result
        ));
    }

    @DeleteMapping("/exception/{ruleId}")
    public ResponseEntity<?> deleteExceptionRule(
            @AuthenticationPrincipal SecurityUser admin,
            @PathVariable Long ruleId) {
        attendanceRuleService.deleteExceptionRule(admin.getCompanyId(), ruleId);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "해당 예외 규칙이 삭제되었습니다.", null));
    }

    @PostMapping("/initialize/{companyId}")
    public ResponseEntity<ResponseDto<Void>> initializeDefaultRule(@PathVariable Long companyId) {
        Company company = companyService.findById(companyId);
        attendanceRuleService.createDefaultAttendanceRule(company);

        return ResponseEntity.ok(new ResponseDto<>(
                HttpStatus.CREATED,
                "회사의 기본 근태 규칙이 성공적으로 초기화되었습니다.",
                null
        ));
    }
}