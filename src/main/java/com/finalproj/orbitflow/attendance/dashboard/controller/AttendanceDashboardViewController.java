package com.finalproj.orbitflow.attendance.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceDashboardViewController
 * @since : 2025. 12. 19. 금요일
 */
@Controller
@RequestMapping("/view/admin/attendance")
public class AttendanceDashboardViewController {

    @GetMapping("/rules")
    public String attendanceRulesPage(Model model) {
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "attendance-rules"); // 규칙 관리 활성화
        return "admin/admin-rule";
    }




}
