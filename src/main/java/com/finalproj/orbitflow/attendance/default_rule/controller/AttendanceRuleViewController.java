package com.finalproj.orbitflow.attendance.default_rule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 근태 관련 페이지 뷰 컨트롤러
 */
@Controller
@RequestMapping("/view/admin")
public class AttendanceRuleViewController {

    @GetMapping("/attendance/rules")
    public String attendanceRulesPage(Model model) {
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "attendance-rules"); // 규칙 관리 활성화
        return "admin-attendance/admin-rule";
    }








}

