package com.finalproj.orbitflow.leave.leaveBalance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveBalanceViewController
 * @since : 2025. 12. 22. 월요일
 */

@Controller
@RequestMapping("/view")
public class LeaveBalanceViewController {

    // 연차 현황 조회
    @GetMapping("/attendance/annual-leave")
    public String myLeavePage(Model model) {
        model.addAttribute("currentGNB", "work");
        model.addAttribute("currentMenu", "annual-leave");
        model.addAttribute("pageTitle", "내 연차 현황");
        return "leave/annual-leave";
    }
    
    // 휴가 신청 현황 조회
    @GetMapping("/attendance/leave-history")
    public String leaveHistoryPage(Model model) {
        model.addAttribute("currentGNB", "work");
        model.addAttribute("currentMenu", "leave-history");
        model.addAttribute("pageTitle", "휴가 신청 현황");
        return "leave/leave-history";
    }

}
