package com.finalproj.orbitflow.reservation.controller;

import com.finalproj.orbitflow.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationController
 * @since : 2025-12-22 오후 2:19 월요일
 */

@RequestMapping("/api")
@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
}
