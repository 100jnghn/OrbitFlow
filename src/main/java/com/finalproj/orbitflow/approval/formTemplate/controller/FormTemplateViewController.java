package com.finalproj.orbitflow.approval.formTemplate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FormTemplateViewController {

    /**
     * 결재 양식 관리(어드민) 홈 화면 반환
     */
    @GetMapping("/approval-home")
    public String approvalHomeView() {
        // templates/approval-home/approval-home.html 반환
        return "approval-home/approval-home";
    }
}
