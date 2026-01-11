package com.finalproj.orbitflow.hr.logAudit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuditLogViewController
 * @since : 2026-01-06 화요일
 */
@Controller
@RequestMapping("/view/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogViewController {

    @GetMapping
    public String listPage(org.springframework.ui.Model model) {
        model.addAttribute("currentMenu", "audit-log");
        return "admin/audit-log/list";
    }

    @GetMapping("/detail")
    public String detailPage(org.springframework.ui.Model model) {
        model.addAttribute("currentMenu", "audit-log");
        return "admin/audit-log/detail";
    }
}
