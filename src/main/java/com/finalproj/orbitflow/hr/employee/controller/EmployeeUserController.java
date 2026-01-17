package com.finalproj.orbitflow.hr.employee.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.employee.dto.EmployeeDetailResDto;
import com.finalproj.orbitflow.hr.employee.dto.EmployeeSearchDto;
import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeUserController
 * @since : 2025-12-30 화요일
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeUserController {

    private final EmployeeService employeeService;

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EmployeeSearchDto>> searchEmployees(@RequestParam String keyword) {
        // Validation: 최소 2자, 최대 30자, trim 처리
        String trimmedKeyword = keyword != null ? keyword.trim() : "";
        if (trimmedKeyword.length() < 2) {
            throw new IllegalArgumentException("검색어는 최소 2자 이상이어야 합니다.");
        }
        if (trimmedKeyword.length() > 30) {
            throw new IllegalArgumentException("검색어는 30자 이하여야 합니다.");
        }
        return ResponseEntity.ok(
                employeeService.searchEmployees(
                        SecurityUtils.getCompanyId(),
                        keyword.trim()
                )
        );
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<ResponseDto<EmployeeDetailResDto>> detail(
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "사원 상세 조회 성공",
                        employeeService.getDetail(
                                SecurityUtils.getCompanyId(),
                                employeeId
                        )
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto> getEmployee(
            @AuthenticationPrincipal SecurityUser user
    ) {
        Long companyId = user.getCompanyId();
        Long employeeId = user.getEmployeeId();
        EmployeeDetailResDto employee = employeeService.getDetail(companyId, employeeId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "사원 정보 조회 성공", employee)
        );
    }


    @GetMapping("/by-org-and-position")
    public ResponseEntity<?> listByOrgAndPosition(
            @RequestParam Long orgId,
            @RequestParam Long positionCategoryId
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "조직/직책별 사원 조회 성공",
                        employeeService.findByOrgAndPosition(SecurityUtils.getCompanyId(), orgId, positionCategoryId)
                )
        );
    }
}