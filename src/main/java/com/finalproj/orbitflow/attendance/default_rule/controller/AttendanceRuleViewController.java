package com.finalproj.orbitflow.attendance.default_rule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 근태 관련 페이지 뷰 컨트롤러
 */
@Controller
@RequestMapping("/view/admin/attendance")
public class AttendanceRuleViewController {


    @GetMapping("/dashboard")
    public String attendanceDashboard(Model model) {
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "attendance-dashboard"); // 대시보드 활성화
        return "admin/admin-dashboard";
    }





}

