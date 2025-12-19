package com.finalproj.orbitflow.approval.formTemplate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/view")
public class FormTemplateViewController {

    /**
     * 결재 양식 관리(어드민) 홈 화면 반환
     */
    @GetMapping("/admin/approval")
    public String approvalHomeView() {
        // templates/approval-home/approval-home.html 반환
        return "approval-home/approval-home";
    }

    @GetMapping("/admin/create-template")
    public String createTemplateView(
            @RequestParam Long groupId,
            @RequestParam Long templateId,
            Model model
    ) {
        model.addAttribute("groupId", groupId);
        model.addAttribute("templateId", templateId);
        return "form-template/createTemplate";
    }


}
