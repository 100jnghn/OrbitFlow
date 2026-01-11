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
 * @filename : OrgSidebarController
 * @since : 2026-01-05 월요일
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sidebar")
@PreAuthorize("isAuthenticated()")
public class OrgSidebarController {

    private final OrgSidebarService orgSidebarService;

    @GetMapping("/extensions")
    public ResponseEntity<ResponseDto<List<OrgSidebarDto>>> sidebarExtensions() {
        return ResponseEntity.ok(
                new ResponseDto<>(
                        HttpStatus.OK,
                        "사이드바 내선 조직도 조회",
                        orgSidebarService.getSidebarTree(SecurityUtils.getCompanyId())
                )
        );
    }
}
