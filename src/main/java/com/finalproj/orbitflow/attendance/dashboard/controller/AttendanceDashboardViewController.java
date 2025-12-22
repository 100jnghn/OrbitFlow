package com.finalproj.orbitflow.attendance.dashboard.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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
@RequestMapping("/view")
public class AttendanceDashboardViewController {

    @GetMapping("/admin/attendance")
    public String attendanceDashboardPage() {
        return "admin/admin-dashboard";
    }
}
