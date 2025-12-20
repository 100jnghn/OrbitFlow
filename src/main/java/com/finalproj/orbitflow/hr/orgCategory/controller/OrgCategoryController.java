package com.finalproj.orbitflow.hr.orgCategory.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryCreateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryUpdateReqDto;
import com.finalproj.orbitflow.hr.orgCategory.service.OrgCategoryService;
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
    public ResponseEntity<ResponseDto> create(
            @RequestBody @Valid OrgCategoryCreateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();

        Long id = orgCategoryService.create(companyId, request.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(
                        HttpStatus.CREATED,
                        "결재 양식 초안이 생성되었습니다.",
                        id));

    }


    @GetMapping
    public ResponseEntity<ResponseDto> list() {
        Long companyId = SecurityUtils.getCompanyId();

        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        "조직 카테고리 목록 조회",
                        orgCategoryService.findAll(companyId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid OrgCategoryUpdateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();

        orgCategoryService.update(companyId, id, request.getName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(HttpStatus.OK,
                        "조직 카테고리가 수정되었습니다.",
                        null));
    }


    @PutMapping("/order")
    public ResponseEntity<ResponseDto> updateOrder(
            @RequestBody @Valid OrgCategoryOrderUpdateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();
        orgCategoryService.updateOrder(companyId, request.getOrders());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto(HttpStatus.OK,
                        "조직 카테고리의 순서가 일괄 수정되었습니다.",
                        null));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deactivate(
            @PathVariable Long id
    ) {
        Long companyId = SecurityUtils.getCompanyId();

        orgCategoryService.deactivate(companyId, id);
        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "조직 카테고리를 비활성화 하였습니다.", null)
        );    }
}
