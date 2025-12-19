package com.finalproj.orbitflow.approval.formTemplate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FormTemplateViewController {

    /**
     * 결재 양식 관리(어드민) 홈 화면 반환
     */
    @GetMapping("/admin/approval")
    public String approvalHomeView(Model model) {
        model.addAttribute("pageTitle", "결재 양식 관리");
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "approval");
        // templates/approval-home/approval-home.html 반환
        return "approval-home/approval-home";
    }
}
