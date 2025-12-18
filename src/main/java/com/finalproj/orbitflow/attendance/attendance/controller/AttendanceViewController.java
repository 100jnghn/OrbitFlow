package com.finalproj.orbitflow.attendance.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 근태(출퇴근 기록) 화면을 반환하는 뷰 컨트롤러
 */
@Controller
@RequestMapping("/attendance")
public class AttendanceViewController {

    /**
     * 근태 메인 페이지
     * - 템플릿: templates/attendance/attendance.html
     */
    @GetMapping
    public String attendancePage() {
        return "attendance/attendance";
    }
}


