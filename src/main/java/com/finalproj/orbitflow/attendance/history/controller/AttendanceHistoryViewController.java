package com.finalproj.orbitflow.attendance.history.controller;

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
@RequestMapping("/view/attendance")
public class AttendanceHistoryViewController {

    @GetMapping("/monthly")
    public String monthlyHistoryPage(Model model) {
        model.addAttribute("currentMenu", "monthly");
        return "/attendance/history";
    }
}
