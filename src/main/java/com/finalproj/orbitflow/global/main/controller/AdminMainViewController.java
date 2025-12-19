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
     * 관리자 대시보드 (사이드바 없음)
     */
    @GetMapping("/admin")
    public String adminMainPage(Model model) {
        model.addAttribute("pageTitle", "관리자 대시보드");
        model.addAttribute("currentGNB", "admin");
        return "admin/admin-main";
    }

    /**
     * 관리자 메뉴 페이지 (사이드바 있음)
     */
    @GetMapping("/admin/menu")
    public String adminMenuPage(Model model) {
        model.addAttribute("pageTitle", "관리자 메뉴");
        model.addAttribute("currentGNB", "admin");
        return "admin/admin-menu";
    }
}

