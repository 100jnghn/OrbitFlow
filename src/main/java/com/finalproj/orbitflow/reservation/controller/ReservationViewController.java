package com.finalproj.orbitflow.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationViewController
 * @since : 2025-12-22 오후 9:26 월요일
 */
@RequestMapping("/view/reservation")
@Controller
public class ReservationViewController {

    @GetMapping("/me")
    public String getMyReservationPage() {
        return "reservation/my-reservation";
    }

}
