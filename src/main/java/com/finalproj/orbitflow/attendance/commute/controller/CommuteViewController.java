package com.finalproj.orbitflow.attendance.commute.controller;

import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 근태(출퇴근 기록) 화면을 반환하는 뷰 컨트롤러
 */
@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
public class CommuteViewController {

    private final CompanyRepository companyRepository;

    @GetMapping("/attendance/commute")
    public String commutePage(Model model, @AuthenticationPrincipal SecurityUser user) {
        if (user != null) {
            companyRepository.findById(user.getCompanyId())
                    .ifPresent(company -> model.addAttribute("companyName", company.getName()));
        }
        model.addAttribute("currentMenu", "commute");
        return "attendance/commute";
    }

}


