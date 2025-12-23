package com.finalproj.orbitflow.hr.orgCategory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 /**
 * 조직 카테고리 화면(View) 컨트롤러
 * - 관리자 전용 화면
 * - Thymeleaf 템플릿 반환 전용
 *
 * URL 규칙:
 * /view/admin/org-categories
 *
 *
 * @author : seunga03
 * @filename : OrgCategoryViewController
 * @since : 2025-12-23 화요일
 */
@Controller
@RequestMapping("/view/admin/org-categories")
public class OrgCategoryViewController {

    /**
     * 조직 카테고리 관리 목록 화면
     */
    @GetMapping
    public String orgCategoryList(Model model) {

        model.addAttribute("pageTitle", "조직 카테고리 관리");
        model.addAttribute("currentMenu", "org-category");
        model.addAttribute("sidebarFragment", "admin-sidebar");

        return "admin/hr/org-category/list";
    }
}
