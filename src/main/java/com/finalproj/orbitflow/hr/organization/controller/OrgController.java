package com.finalproj.orbitflow.hr.organization.controller;

import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.organization.dto.OrgCreateReqDto;
import com.finalproj.orbitflow.hr.organization.dto.OrgOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.organization.dto.OrgResDto;
import com.finalproj.orbitflow.hr.organization.dto.OrgUpdateReqDto;
import com.finalproj.orbitflow.hr.organization.service.OrgService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 조직 관리 컨트롤러 (관리자 전용)
 *
 * @author : seunga03
 * @filename : OrgController
 * @since : 2025-12-19 금요일
 */
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/organizations")
public class OrgController {

    private final OrgService orgService;

    @PostMapping
    public ResponseEntity<Long> create(
            @RequestBody @Valid OrgCreateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();
        Long id = orgService.create(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping
    public ResponseEntity<List<OrgResDto>> list() {
        Long companyId = SecurityUtils.getCompanyId();
        return ResponseEntity.ok(orgService.findAll(companyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody @Valid OrgUpdateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();
        orgService.update(companyId, id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/order")
    public ResponseEntity<Void> updateOrder(
            @RequestBody @Valid OrgOrderUpdateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();
        orgService.updateOrder(companyId, request.getOrders());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        Long companyId = SecurityUtils.getCompanyId();
        orgService.deactivate(companyId, id);
        return ResponseEntity.ok().build();
    }
}
