package com.finalproj.orbitflow.hr.position.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.position.dto.PositionOrderUpdateReqDto;
import com.finalproj.orbitflow.hr.position.dto.PositionReqDto;
import com.finalproj.orbitflow.hr.position.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionController
 * @since : 2025-12-22 월요일
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/positions")
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<ResponseDto> create(
            @RequestBody @Valid PositionReqDto request
    ) {
        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.CREATED,
                        "직책 생성 완료",
                        positionService.create(SecurityUtils.getCompanyId(), request)
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
                        "직책 목록 조회 완료",
                        positionService.findAll(SecurityUtils.getCompanyId(), includeInactive)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid PositionReqDto request
    ) {
        positionService.update(SecurityUtils.getCompanyId(), id, request);
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "직책 수정 완료",
                        null
                )
        );
    }

    @PostMapping("/order")
    public ResponseEntity<ResponseDto> updateOrder(
            @RequestBody @Valid PositionOrderUpdateReqDto request
    ) {
        positionService.updateOrder(SecurityUtils.getCompanyId(), request.getOrders());

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "직책 순서 수정 완료", null)
        );
    }
}
