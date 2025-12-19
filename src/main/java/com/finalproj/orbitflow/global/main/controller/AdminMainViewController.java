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

    @GetMapping("/admin")
    public String adminMainPage(Model model) {
        model.addAttribute("currentGNB", "admin");
        return "admin/admin-main";
    }
}

