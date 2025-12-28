package com.finalproj.orbitflow.hr.organization.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.organization.dto.*;
import com.finalproj.orbitflow.hr.organization.service.OrgService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
    public ResponseEntity<ResponseDto<List<OrgResDto>>> list(
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        Long companyId = SecurityUtils.getCompanyId();

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "조직 목록 조회",
                        orgService.findAll(companyId, includeInactive)
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
                new ResponseDto(HttpStatus.OK, "조직 정보 변경 완료", null)
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

    // 프론트에서 사용하지 않는 api -> 추후 목록에서 바로 비활성화 같은 기능용으로 일단 보류
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deactivate(@PathVariable Long id) {
        Long companyId = SecurityUtils.getCompanyId();
        orgService.deactivate(companyId, id);
        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "조직 비활성화 완료", null));
    }

    // 특정 조직 카테고리에 해당하는 조직들 조회
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<ResponseDto<List<OrgResDto>>> listByCategory(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "조직 카테고리별 조직 조회 성공",
                        orgService.findByCategory(SecurityUtils.getCompanyId(), categoryId)
                )
        );
    }


    @GetMapping("/include-orgs")
    public ResponseEntity<ResponseDto> listByIncludeOrgs() {
        log.info("[include-orgs] API called");


        log.info("[include-orgs] currentUser = {}", SecurityUtils.getCurrentUser());


        List<OrgResDto> orgsByEmployeeId = orgService.findOrgsByEmployeeId(SecurityUtils.getCurrentUser().getOrganizationId());
        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "소속 조직도 조회 성공", orgsByEmployeeId)
        );
    }

    @GetMapping("/{id}/deactivate-check")
    public ResponseEntity<ResponseDto<OrgDeactivateCheckResDto>> checkDeactivate(
            @PathVariable Long id
    ) {
        Long companyId = SecurityUtils.getCompanyId();

        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "비활성화 가능 여부 조회",
                        orgService.checkDeactivatable(companyId, id)
                )
        );
    }

}

