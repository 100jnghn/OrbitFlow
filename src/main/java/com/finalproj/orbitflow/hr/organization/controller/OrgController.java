package com.finalproj.orbitflow.hr.organization.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
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
    public ResponseEntity<ResponseDto> create(
            @RequestBody @Valid OrgCreateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();
        Long id = orgService.create(companyId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto<>(
                        HttpStatus.CREATED,
                        "조직 생성 완료",
                        id
                ));
    }

    @GetMapping
    public ResponseEntity<ResponseDto<List<OrgResDto>>> list() {
        Long companyId = SecurityUtils.getCompanyId();

        return ResponseEntity.ok(
                new ResponseDto(
                        HttpStatus.OK,
                        "조직 목록 조회",
                        orgService.findAll(companyId)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> update(
            @PathVariable Long id,
            @RequestBody @Valid OrgUpdateReqDto request
    ) {
        orgService.update(SecurityUtils.getCompanyId(), id, request);
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "조직 수정 완료", null)
        );
    }

    @PutMapping("/order")
    public ResponseEntity<ResponseDto<Void>> updateOrder(
            @RequestBody @Valid OrgOrderUpdateReqDto request
    ) {
        Long companyId = SecurityUtils.getCompanyId();
        orgService.updateOrder(companyId, request.getOrders());
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "조직 순서 수정 완료", null)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deactivate(@PathVariable Long id) {
        Long companyId = SecurityUtils.getCompanyId();
        orgService.deactivate(companyId, id);
        return ResponseEntity.ok(
        new ResponseDto<>(HttpStatus.OK, "조직 비활성화 완료", null));
    }
}
