package com.finalproj.orbitflow.attendance.commute.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 근태(출퇴근 기록) 화면을 반환하는 뷰 컨트롤러
 */
@Controller
@RequestMapping("/view")
public class CommuteViewController {

    @GetMapping
    public String commutePage() {
        // templates/attendance/commute.html 파일을 반환한다고 가정합니다.
        return "attendance/commute";
    }

}


