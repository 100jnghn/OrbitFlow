package com.finalproj.orbitflow.attendance.monthly_history.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceHistoryViewController
 * @since : 2025. 12. 21. 일요일
 */

@Controller
@RequestMapping("/view")
public class AttendanceHistoryViewController {

    @GetMapping("/attendance/monthly")
    public String monthlyHistoryPage(Model model) {
        model.addAttribute("currentMenu", "monthly");
        return "attendance/monthly-attendance";
    }
}
