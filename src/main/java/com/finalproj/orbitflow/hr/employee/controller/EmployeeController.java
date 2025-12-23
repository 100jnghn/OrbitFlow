package com.finalproj.orbitflow.hr.employee.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.employee.dto.EmployeeResDto;
import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
 * @filename : EmployeeController
 * @since : 2025-12-23 화요일
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/employees")
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/by-org-and-position")
    public ResponseEntity<ResponseDto<List<EmployeeResDto>>> listByOrgAndPosition(
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
