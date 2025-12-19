package com.finalproj.orbitflow.approval.document.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 결재 관리 화면을 반환하는 View 컨트롤러
 */
@Controller
@RequestMapping("/view/admin")
public class ApprovalViewController {

    /**
     * 결재 관리 메인 화면 반환 (결재 문서 목록)
     */
    @GetMapping("/approval")
    public String approvalManagementView(Model model) {
        model.addAttribute("pageTitle", "결재 관리");
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "approval");
        return "approval/approval-list";
    }
}

