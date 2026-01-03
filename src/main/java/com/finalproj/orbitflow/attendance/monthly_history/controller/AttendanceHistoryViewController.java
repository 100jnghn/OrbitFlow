package com.finalproj.orbitflow.attendance.monthly_history.controller;

import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequiredArgsConstructor
public class AttendanceHistoryViewController {

    private final CompanyRepository companyRepository;

    @GetMapping("/attendance/monthly")
    public String monthlyHistoryPage(Model model, @AuthenticationPrincipal SecurityUser user) {
        if (user != null) {
            companyRepository.findById(user.getCompanyId())
                    .ifPresent(company -> model.addAttribute("companyName", company.getName()));
        }
        model.addAttribute("currentMenu", "monthly");
        return "attendance/monthly-attendance";
    }
}
