package com.finalproj.orbitflow.attendance.attendanceRule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 근태 관련 페이지 뷰 컨트롤러
 */
@Controller
@RequestMapping("/view")
public class AttendanceRuleViewController {

    @GetMapping("/admin/attendance-rules")
    public String attendanceRulesPage(org.springframework.ui.Model model) {
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "attendance-rules");
        return "admin/admin-rule";
    }



}

