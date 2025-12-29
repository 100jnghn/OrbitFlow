package com.finalproj.orbitflow.leave.leaveGrant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveGrantViewController
 * @since : 2025. 12. 24. 수요일
 */

@Controller
@RequestMapping("/view")
public class LeaveGrantViewController {

    // 기타 휴가 조회
    @GetMapping("/attendance/other-leave")
    public String otherLeavePage(Model model) {
        model.addAttribute("currentGNB", "work");
        model.addAttribute("currentMenu", "other-leave");

        return "leave/other-leave";
    }
}
