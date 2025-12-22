package com.finalproj.orbitflow.reservation.controller;

import com.finalproj.orbitflow.reservation.service.ReservationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationStatusController
 * @since : 2025-12-22 오후 2:20 월요일
 */
@RequestMapping("/api")
@Controller
@RequiredArgsConstructor
public class ReservationStatusController {

    private final ReservationStatusService reservationStatusService;
}
