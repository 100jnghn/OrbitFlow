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
        // 공통 레이아웃에 필요한 사용자/회사 정보
        model.addAttribute("companyName", "엠터캠피스");
        model.addAttribute("userName", user != null ? user.getUsername() : "사용자");

        // 헤더(GNB)에서 활성화할 탭
        model.addAttribute("currentGNB", "work");

        // 사이드바(LNB)에서 강조할 메뉴 ID
        model.addAttribute("currentMenu", "attendance-rules");

        // layout.html 의 sidebar-shell 에 주입할 사이드바 파일 경로
        model.addAttribute("sidebarFragment", "sidebar/attendance-sidebar :: menu");

        // 실제 콘텐츠가 담긴 메인 페이지 리턴
        return "form-template/attendence_exception";
    }

//
//    @GetMapping("/admin/attendance-rules")
//    public String attendanceRulesPage(Model model, @AuthenticationPrincipal SecurityUser user) {
//        // 1. 공통 레이아웃에 필요한 사용자 정보
//        model.addAttribute("companyName", "엠터캠피스");
//
//        // 2. 헤더(GNB)의 '관리자' 메뉴 활성화 (selected 클래스 추가용)
//        model.addAttribute("currentGNB", "admin");
//
//        // 3. 사이드바(LNB)에서 강조할 메뉴 ID
//        model.addAttribute("currentMenu", "attendance-rules");
//
//        // 4. layout.html의 sidebar-shell에 주입할 사이드바 파일 경로
//        // 형식: "파일경로 :: 프래그먼트이름"
//        model.addAttribute("sidebarFragment", "sidebar/attendance-sidebar :: menu");
//
//        // 5. 실제 콘텐츠가 담긴 메인 페이지 리턴
//        return "form-template/attendence_exception";
//    }

    /**
     * 출퇴근 페이지
     */
    @GetMapping("/attendance/commute")
    public String commutePage() {
        return "form-template/commute";
    }

    /**
     * 월별 근태 조회 페이지
     */
    @GetMapping("/attendance/monthly")
    public String monthlyAttendancePage() {
        return "form-template/monthly-attendance";
    }



}

