package com.finalproj.orbitflow.global.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 메인 페이지 View Controller
 */
@Controller
@RequestMapping("/view")
public class AdminMainViewController {

    /**
     * 관리자 진입점 → 사원 관리로 바로 이동
     */
    @GetMapping("/admin")
    public String adminRoot() {
        return "redirect:/view/admin/employees";
    }

    /**
     * 관리자 메인 (사이드바 포함)
     */
    @GetMapping("/admin/menu")
    public String adminMenuPage(Model model) {
        model.addAttribute("pageTitle", "관리자");
        model.addAttribute("currentGNB", "admin");
        return "admin/admin-menu";
    }
}