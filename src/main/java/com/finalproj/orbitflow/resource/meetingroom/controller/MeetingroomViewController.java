package com.finalproj.orbitflow.resource.meetingroom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : MeetingroomViewController
 * @since : 2025-12-18 오후 11:03 목요일
 */
@RequestMapping("/view/resource")
@Controller
public class MeetingroomViewController {

    @GetMapping("/admin/meetingrooms")
    public String getMeetingroomsPage(Model model) {

        model.addAttribute("currentGNB", "admin");

        return "admin-meetingroom/admin-meetingrooms";
    }

    // 사용자 회의실 조회 화면
    @GetMapping("/meetingrooms")
    public String meetingroomList() {
        return "meetingroom/meetingroom-list";
    }
}
