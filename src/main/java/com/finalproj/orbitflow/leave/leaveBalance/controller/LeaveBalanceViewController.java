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

    /**
     * 내 연차 조회 화면 이동
     */
    @GetMapping("/attendance/annual-leave")
    public String myLeavePage(Model model) {

        model.addAttribute("currentGNB", "work");
        model.addAttribute("currentMenu", "annual-leave");
        return "leave/my-leave";
    }
}
