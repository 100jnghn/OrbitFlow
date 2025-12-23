package com.finalproj.orbitflow.hr.orgPositionUsage.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.orgPositionUsage.dto.OrgPositionPolicyUpdateReqDto;
import com.finalproj.orbitflow.hr.orgPositionUsage.service.OrgPositionUsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgPositionUsageController
 * @since : 2025-12-23 화요일
 */
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMIN')")
@RequestMapping("/api/admin/org-position-policies")
public class OrgPositionUsageController {

    private final OrgPositionUsageService service;

    @GetMapping("/{orgId}")
    public ResponseEntity<ResponseDto<?>> getPolicy(
            @PathVariable Long orgId
    ) {
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "조직별 직책 정책 조회 성공", service.findByOrg(SecurityUtils.getCompanyId(), orgId))
        );
    }

    @PutMapping
    public ResponseEntity<ResponseDto<?>> updatePolicy(
            @RequestBody @Valid OrgPositionPolicyUpdateReqDto request
    ) {
        service.updatePolicy(SecurityUtils.getCompanyId(), request);
        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "조직별 직책 정책 저장 완료", null)
        );
    }
}
