package com.finalproj.orbitflow.hr.organization.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.organization.dto.OrgResDto;
import com.finalproj.orbitflow.hr.organization.service.OrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : OrgUserController
 * @since : 2025-12-29 오전 11:50 월요일
 */
@RequestMapping("/api/organizations")
@Controller
@RequiredArgsConstructor
public class OrgUserController {

    private final OrgService orgService;

    @GetMapping("/include-orgs")
    public ResponseEntity<ResponseDto> getUserOrgs(
            @AuthenticationPrincipal SecurityUser user
    ) {
        List<OrgResDto> orgsByEmployeeId = orgService.findOrgsByEmployeeId(SecurityUtils.getCurrentUser().getOrganizationId());

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "소속 조직도 조회 성공", orgsByEmployeeId)
        );
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

    @GetMapping
    public ResponseEntity<ResponseDto<List<OrgResDto>>> listOrSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean includeDescendants
    ) {
        Long companyId = SecurityUtils.getCompanyId();

        // 검색어 없으면 → 사용자 기준 전체 조직 (회사 제외)
        if (keyword == null || keyword.isBlank()) {
            List<OrgResDto> orgs = orgService.findAll(companyId, false)
                    .stream()
                    .filter(o -> o.getParentOrgId() != null)
                    .toList();

            return ResponseEntity.ok(
                    new ResponseDto<>(
                            HttpStatus.OK,
                            "조직 목록 조회",
                            orgs
                    )
            );
        }

        // 검색
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "조직 검색 결과 조회",
                        orgService.searchForUser(
                                companyId,
                                keyword,
                                includeDescendants
                        )
                )
        );
    }

}
