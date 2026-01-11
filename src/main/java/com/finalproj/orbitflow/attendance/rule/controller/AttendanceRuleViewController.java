package com.finalproj.orbitflow.attendance.rule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/view/admin")
public class AttendanceRuleViewController {

    @GetMapping("/attendance/rules")
    public String attendanceRulesPage(Model model) {
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "attendance-rules");
        return "admin-attendance/admin-rule";
    }








}

