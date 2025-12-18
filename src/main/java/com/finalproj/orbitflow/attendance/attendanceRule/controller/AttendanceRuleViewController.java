package com.finalproj.orbitflow.attendance.attendanceRule.controller;

import com.finalproj.orbitflow.global.security.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 근태 관련 페이지 뷰 컨트롤러
 */
@Controller
@RequestMapping("/api")
public class AttendanceRuleViewController {

    /**
     * 근태 규칙 및 근무 시간 관리 페이지
     */
//    @GetMapping("/admin/attendance-rules")
//    public String attendanceRulesPage() {
//        return "form-template/attendence_exception";
//    }

    @GetMapping("/admin/attendance-rules")
    public String attendanceRulesPage(Model model, @AuthenticationPrincipal SecurityUser user) {
        model.addAttribute("companyName", "멀티캠퍼스");
        model.addAttribute("userName", user != null ? user.getUsername() : "김오빗");
        model.addAttribute("pageTitle", "근태 규칙 관리");
        model.addAttribute("currentGNB", "admin");
        model.addAttribute("currentMenu", "attendance-rules");

        // CSS 적용을 위해 이 코드를 반드시 추가해야 합니다.
        model.addAttribute("pageStyles", new String[]{
                "/css/header.css",
                "/css/sidebar.css",
                "/css/attendance_exception.css"
        });

        return "attendance/attendance_exception";
    }

    /**
     * 출퇴근 페이지
     */
    @GetMapping("/attendance/commute")
    public String commutePage(Model model, @AuthenticationPrincipal SecurityUser user) {
        model.addAttribute("companyName", "멀티캠퍼스");
        model.addAttribute("userName", user != null ? user.getUsername() : "김오빗");
        model.addAttribute("pageTitle", "출퇴근");
        model.addAttribute("currentGNB", "work");
        model.addAttribute("currentMenu", "commute");
        return "attendance/commute";
    }

    /**
     * 월별 근태 조회 페이지
     */
    @GetMapping("/attendance/monthly")
    public String monthlyAttendancePage(Model model, @AuthenticationPrincipal SecurityUser user) {
        model.addAttribute("companyName", "멀티캠퍼스");
        model.addAttribute("userName", user != null ? user.getUsername() : "김오빗");
        model.addAttribute("pageTitle", "월별 근태 조회");
        model.addAttribute("currentGNB", "work");
        model.addAttribute("currentMenu", "monthly");
        return "attendance/monthly-attendance";
    }



}

