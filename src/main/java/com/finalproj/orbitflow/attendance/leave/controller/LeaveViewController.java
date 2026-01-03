package com.finalproj.orbitflow.attendance.leave.controller;

import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
public class LeaveViewController {

    private final CompanyRepository companyRepository;

    // 연차 현황 조회
    @GetMapping("/attendance/annual-leave")
    public String myLeavePage(Model model, @AuthenticationPrincipal SecurityUser user) {
        if (user != null) {
            companyRepository.findById(user.getCompanyId())
                    .ifPresent(company -> model.addAttribute("companyName", company.getName()));
        }
        model.addAttribute("currentGNB", "work");
        model.addAttribute("currentMenu", "annual-leave");
        model.addAttribute("pageTitle", "내 연차 현황");
        return "leave/annual-leave";
    }

    // 휴가 신청 현황 조회
    @GetMapping("/attendance/leave-history")
    public String leaveHistoryPage(Model model, @AuthenticationPrincipal SecurityUser user) {
        if (user != null) {
            companyRepository.findById(user.getCompanyId())
                    .ifPresent(company -> model.addAttribute("companyName", company.getName()));
        }
        model.addAttribute("currentGNB", "work");
        model.addAttribute("currentMenu", "leave-history");
        model.addAttribute("pageTitle", "휴가 신청 현황");
        return "leave/leave-history";
    }
}
