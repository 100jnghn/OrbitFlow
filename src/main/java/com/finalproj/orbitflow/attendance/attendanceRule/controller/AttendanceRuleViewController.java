package com.finalproj.orbitflow.attendance.attendanceRule.controller;

import org.springframework.stereotype.Controller;
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
    @GetMapping("/admin/attendance-rules")
    public String attendanceRulesPage() {
        return "form-template/attendence_exception";
    }

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

