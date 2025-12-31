package com.finalproj.orbitflow.hr.orgCategory.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUtils;
import com.finalproj.orbitflow.hr.orgCategory.dto.OrgCategoryResDto;
import com.finalproj.orbitflow.hr.orgCategory.service.OrgCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : OrgCategoryUserController
 * @since : 2025-12-29 오후 5:28 월요일
 */
@RequestMapping("/api/org-categories")
@Controller
@RequiredArgsConstructor
public class OrgCategoryUserController {

    private final OrgCategoryService service;

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
}
