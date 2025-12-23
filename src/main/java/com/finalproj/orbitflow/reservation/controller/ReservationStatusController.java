package com.finalproj.orbitflow.reservation.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.reservation.dto.ReservationStatusResDto;
import com.finalproj.orbitflow.reservation.service.ReservationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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

    @GetMapping("/reservation/status")
    public ResponseEntity<ResponseDto> getReservationStatuses() {

        List<ReservationStatusResDto> list = reservationStatusService.getReservationStatuses();

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "예약 상태 리스트 조회 성공", list)
        );
    }
}
