package com.finalproj.orbitflow.hr.employee.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.employee.dto.*;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import com.finalproj.orbitflow.hr.logAudit.dto.AuditLogResDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자용 사원 관리 API 컨트롤러
 * - 사원 목록 조회 (검색/상태 필터/페이징)
 * - 사원 상세 조회
 * - 사원 생성 및 수정
 * - 결재선 구성을 위한 조직/직책별 사원 조회
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
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMIN')")
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

    @GetMapping
    public ResponseEntity<ResponseDto<Page<EmployeeListResDto>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmployeeStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "사원 목록 조회 성공",
                        employeeService.search(
                                SecurityUtils.getCompanyId(),
                                keyword,
                                status,
                                pageable
                        )
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

    @PostMapping
    public ResponseEntity<ResponseDto<Void>> create(
            @RequestBody @Valid EmployeeCreateReqDto dto
    ) {
        employeeService.create(SecurityUtils.getCompanyId(), dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto<>(HttpStatus.CREATED, "사원 생성 성공", null));
    }


    @GetMapping("/{employeeId}/edit")
    public ResponseEntity<ResponseDto<EmployeeUpdateResDto>> editView(
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "사원 수정 초기값 조회 성공",
                        employeeService.getEditView(SecurityUtils.getCompanyId(), employeeId)
                )
        );
    }



    @PutMapping("/{employeeId}")
    public ResponseEntity<ResponseDto<Void>> update(
            @PathVariable Long employeeId,
            @RequestBody @Valid EmployeeUpdateReqDto dto
    ) {
        employeeService.update(SecurityUtils.getCompanyId(), employeeId, dto);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "사원 수정 성공", null));
    }



    @PutMapping("/{employeeId}/status")
    public ResponseEntity<ResponseDto<Void>> updateStatus(
            @PathVariable Long employeeId,
            @RequestBody EmployeeStatusUpdateReqDto dto
    ) {
        employeeService.updateStatus(
                SecurityUtils.getCompanyId(),
                employeeId,
                dto.getStatus()
        );
        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "상태 변경 성공", null)
        );
    }


    @GetMapping("/{employeeId}/logs")
    public ResponseEntity<ResponseDto<List<AuditLogResDto>>> logs(
            @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "사원 변경 이력 조회 성공",
                        employeeService.getEmployeeAuditLogs(
                                SecurityUtils.getCompanyId(),
                                employeeId
                        )
                )
        );
    }


    @GetMapping("/check-email")
    public ResponseEntity<ResponseDto<Map<String, Boolean>>> checkEmployeeEmail(
            @RequestParam String email
    ) {
        boolean available = employeeService.isEmailAvailable(
                SecurityUtils.getCompanyId(),
                email
        );

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "사원 이메일 중복 확인 완료",
                        Map.of("available", available)
                )
        );
    }

}
