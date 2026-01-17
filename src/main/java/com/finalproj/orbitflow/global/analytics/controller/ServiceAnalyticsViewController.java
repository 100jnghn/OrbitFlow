package com.finalproj.orbitflow.global.analytics.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view")
public class ServiceAnalyticsViewController {

    @GetMapping("/service-analytics")
    public String serviceAnalyticsPage(Model model) {
        model.addAttribute("pageTitle", "OrbitFlow 서비스 현황 Analytics");
        // GNB나 사이드바 선택 효과를 위해 currentGNB 설정 (필요시)
        model.addAttribute("currentGNB", "analytics");
        return "admin/service-analytics";
    }
}
