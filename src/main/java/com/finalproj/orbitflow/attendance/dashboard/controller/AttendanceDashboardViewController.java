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
@RequestMapping("/view/admin")
public class AttendanceDashboardViewController {



    @GetMapping("/attendance/dashboard")
    public String attendanceDashboard(Model model) {
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "attendance-dashboard"); // 대시보드 활성화
        return "admin-attendance/dashboard";
    }




}
