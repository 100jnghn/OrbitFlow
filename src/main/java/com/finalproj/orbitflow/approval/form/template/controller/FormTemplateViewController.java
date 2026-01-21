package com.finalproj.orbitflow.approval.form.template.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/view")
public class FormTemplateViewController {

    /**
     * 결재 양식 관리(어드민) 홈 화면 반환
     */
    @GetMapping("/admin/approval")
    public String approvalHomeView(Model model) {
        model.addAttribute("pageTitle", "결재 양식 관리");
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "approval");
        // templates/approval-home/home.html 반환
        return "admin-approval/home";
    }

    @GetMapping("/admin/create-template")
    public String createTemplateView(
            @RequestParam Long templateId,
            Model model
    ) {
        model.addAttribute("templateId", templateId);
        return "admin-form-template/build-structure";
    }


    @GetMapping("/admin/approval-rule")
    public String approvalRuleView(
            @RequestParam Long templateId,
            Model model
    ) {
        model.addAttribute("pageTitle", "결재 규칙 생성");
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "approval");
        model.addAttribute("templateId", templateId);
        return "admin-form-template/build-rule";
    }



    @GetMapping("/admin/preview-template/{templateId}")
    public String previewTemplateView(
            @PathVariable Long templateId,
            Model model
    ) {
        model.addAttribute("pageTitle", "결재 양식 미리보기");
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "approval");
        model.addAttribute("templateId", templateId);
        return "admin-form-template/preview";
    }
}
