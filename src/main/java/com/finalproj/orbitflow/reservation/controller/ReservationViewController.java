package com.finalproj.orbitflow.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 예약 View Controller
 *
 * @author : 종훈
 * @filename : ReservationViewController
 * @since : 2025-12-22 오후 9:26 월요일
 */
@RequestMapping("/view")
@Controller
public class ReservationViewController {

    // 내 예약 리스트 조회 페이지
    @GetMapping("reservation/me")
    public String getMyReservationPage(Model model) {
        model.addAttribute("pageTitle", "내 예약 현황");
        model.addAttribute("currentGNB", "reservation");
        model.addAttribute("currentMenu", "my-reservation");
        return "reservation/my-reservation";
    }

    // 회의실 예약 페이지
    @GetMapping("reservation/meetingroom")
    public String getMeetingReservationPage(Model model) {
        model.addAttribute("pageTitle", "회의실 예약");
        model.addAttribute("currentGNB", "reservation");
        model.addAttribute("currentMenu", "meeting-reservation");
        return "reservation/meetingroom-reservation";
    }

    // 차량 예약 페이지
    @GetMapping("reservation/car")
    public String getCarReservationPage(Model model) {
        model.addAttribute("pageTitle", "차량 예약");
        model.addAttribute("currentGNB", "reservation");
        model.addAttribute("currentMenu", "car-reservation");
        return "reservation/car-reservation";
    }

    // 기타 자원 예약 페이지
    @GetMapping("reservation/item")
    public String getItemReservationPage(Model model) {
        model.addAttribute("pageTitle", "기타 자원 예약");
        model.addAttribute("currentGNB", "reservation");
        model.addAttribute("currentMenu", "item-reservation");
        return "reservation/item-reservation";
    }

    // 관리자 - 예약 현황 조회 페이지
    @GetMapping("/admin/reservations")
    public String getAdminReservationsPage() {
        return "admin-reservation/admin-reservations";
    }
}