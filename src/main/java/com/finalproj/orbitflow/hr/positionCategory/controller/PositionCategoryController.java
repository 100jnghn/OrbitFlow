package com.finalproj.orbitflow.hr.positionCategory.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.positionCategory.dto.PositionCategoryOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.positionCategory.dto.PositionCategoryReqDto;
import com.finalproj.orbitflow.hr.positionCategory.service.PositionCategoryService;
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
 * @filename : PositionCategoryController
 * @since : 2025-12-22 월요일
 */
@PreAuthorize("hasAnyRole('ADMIN','COMPANY_ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/position-categories")
public class PositionCategoryController {

    private final PositionCategoryService positionCategoryService;

    @PostMapping
    public ResponseEntity<ResponseDto> create(
            @RequestBody @Valid PositionCategoryReqDto request
    ) {
        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.CREATED,
                        "직책 카테고리 생성 완료",
                        positionCategoryService.create(SecurityUtils.getCompanyId(), request)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ResponseDto> findAll(
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "직책 카테고리 전체 조회 완료",
                        positionCategoryService.findAllWithAssignedCount(
                                SecurityUtils.getCompanyId(),
                                includeInactive
                        )
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid PositionCategoryReqDto request
    ) {
        positionCategoryService.update(SecurityUtils.getCompanyId(), id, request);

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "직책 카테고리 수정 완료",
                        null
                )
        );
    }

    @PutMapping("/order")
    public ResponseEntity<ResponseDto> updateOrder(
            @RequestBody @Valid PositionCategoryOrderUpdateReqDto request
    ) {
        positionCategoryService.updateOrder(
                SecurityUtils.getCompanyId(),
                request.getOrders()
        );
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "직책 카테고리 순서 수정 완료",
                        null
                )
        );
    }
}
