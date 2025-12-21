package com.finalproj.orbitflow.attendance.commute.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 근태(출퇴근 기록) 화면을 반환하는 뷰 컨트롤러
 */
@Controller
@RequestMapping("/view")
public class CommuteViewController {

    @GetMapping("/attendance/commute")
    public String commutePage(Model model) {
        model.addAttribute("currentMenu", "commute");
        return "attendance/commute";
    }

}


