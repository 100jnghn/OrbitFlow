package com.finalproj.orbitflow.hr.employee.controller;

import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.employee.dto.EmployeeSearchDto;
import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}