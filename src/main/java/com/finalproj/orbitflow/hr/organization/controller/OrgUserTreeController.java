package com.finalproj.orbitflow.hr.organization.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.organization.dto.sidebar.OrgSidebarDto;
import com.finalproj.orbitflow.hr.organization.service.OrgSidebarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgUserTreeController
 * @since : 2026-01-05 월요일
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/organizations")
@PreAuthorize("isAuthenticated()")
public class OrgUserTreeController {

    private final OrgSidebarService orgSidebarService;

    @GetMapping("/tree")
    public ResponseEntity<ResponseDto<List<OrgSidebarDto>>> getUserOrgTree() {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "사용자 조직도 조회",
                        orgSidebarService.getSidebarTree(SecurityUtils.getCompanyId())
                )
        );
    }
}
