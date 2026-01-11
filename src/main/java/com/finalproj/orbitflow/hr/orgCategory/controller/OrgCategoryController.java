package com.finalproj.orbitflow.hr.orgCategory.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryCreateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryResDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryUpdateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.repository.OrgCategoryRepository;
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

    private final OrgCategoryService service;
    private final OrgCategoryRepository repository;

    @GetMapping
    public ResponseEntity<ResponseDto<List<OrgCategoryResDto>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "조직 카테고리 목록 조회 성공",
                        service.findAll(SecurityUtils.getCompanyId(), keyword, includeInactive)
                )
        );
    }

    @GetMapping("/selectable")
    public ResponseEntity<ResponseDto<List<OrgCategoryResDto>>> listSelectableForOrg() {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "조직 생성용 카테고리 조회",
                        service.findSelectableForOrg(SecurityUtils.getCompanyId())
                )
        );
    }

    @PostMapping
    public ResponseEntity<ResponseDto<Long>> create(
            @RequestBody @Valid OrgCategoryCreateReqDto request
    ) {
        Long id = service.create(SecurityUtils.getCompanyId(), request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto<>(HttpStatus.CREATED, "조직 카테고리 생성 성공", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> update(
            @PathVariable Long id,
            @RequestBody @Valid OrgCategoryUpdateReqDto request
    ) {
        service.update(SecurityUtils.getCompanyId(), id, request);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "조직 카테고리 수정 성공", null));
    }

    @PutMapping("/order")
    public ResponseEntity<ResponseDto<Void>> updateOrder(
            @RequestBody @Valid OrgCategoryOrderUpdateReqDto request
    ) {
        service.updateOrder(SecurityUtils.getCompanyId(), request.getOrders());
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "순서 변경 저장 완료", null));
    }

}
