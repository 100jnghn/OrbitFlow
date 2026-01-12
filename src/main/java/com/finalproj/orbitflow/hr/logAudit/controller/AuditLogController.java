package com.finalproj.orbitflow.hr.logAudit.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.logAudit.dto.AuditLogResDto;
import com.finalproj.orbitflow.hr.logAudit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditLogAdminController
 * @since : 2026-01-06 화요일
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ResponseDto<Page<AuditLogResDto>>> searchAdminAuditLogs(
            Pageable pageable
    ) {
        Page<AuditLogResDto> result =
                auditLogService.searchAdminAuditLogs(
                        SecurityUtils.getCompanyId(),
                        null,
                        pageable
                );

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "관리자 감사 로그 조회 성공",
                        result
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<AuditLogResDto>> detail(
            @PathVariable Long id
    ) {
        AuditLogResDto result =
                auditLogService.findAdminAuditLog(
                        id,
                        SecurityUtils.getCompanyId()
                );

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "감사 로그 상세 조회 성공",
                        result
                )
        );
    }

}
