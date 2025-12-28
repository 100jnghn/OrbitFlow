package com.finalproj.orbitflow.schedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleViewController
 * @since : 2025-12-28 오후 6:00 일요일
 */
@RequestMapping("/view")
@Controller
public class ScheduleViewController {

    @GetMapping("/admin/company-schedule")
    public String getCompanySchedulePage() {
        return "schedule/admin-company-schedule";
    }
}
