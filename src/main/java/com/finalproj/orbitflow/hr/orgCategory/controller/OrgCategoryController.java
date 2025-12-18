package com.finalproj.orbitflow.hr.orgCategory.controller;

import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryCreateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryResDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryUpdateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.service.OrgCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgCategoryController
 * @since : 2025-12-17 수요일
 */
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/org-categories")
public class OrgCategoryController {

    private final OrgCategoryService orgCategoryService;

    @PostMapping
    public ResponseEntity<Long> create(
            @RequestBody @Valid OrgCategoryCreateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();

        Long id = orgCategoryService.create(
                companyId,
                request.getName(),
                request.getOrderIndex()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }


    @GetMapping
    public ResponseEntity<List<OrgCategoryResDto>> list() {
        Long companyId = SecurityUtils.getCompanyId();

        return ResponseEntity.ok(orgCategoryService.findAll(companyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody @Valid OrgCategoryUpdateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();

        orgCategoryService.update(
                companyId,
                id,
                request.getName(),
                request.getOrderIndex()
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id
    ) {
        Long companyId = SecurityUtils.getCompanyId();

        orgCategoryService.deactivate(companyId, id);
        return ResponseEntity.ok().build();
    }
}
