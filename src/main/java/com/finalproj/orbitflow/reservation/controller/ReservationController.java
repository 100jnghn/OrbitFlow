package com.finalproj.orbitflow.reservation.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.reservation.dto.ReservationResDto;
import com.finalproj.orbitflow.reservation.repository.ReservationRepository;
import com.finalproj.orbitflow.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    // 내 예약 조회 (필터 처리)
    @GetMapping("/reservations/me")
    public ResponseEntity<ResponseDto> getMyReservations(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(defaultValue = "false") boolean showPast,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) String typeCode,
            Pageable pageable
    ) {
        Page<ReservationResDto> result = reservationService.searchMyReservations(
                user.getEmployeeId(),
                showPast,
                statusId,
                typeCode,
                pageable
        );

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "내 예약 리스트 조회 성공", result)
        );
    }

    @PatchMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<ResponseDto> cancelReservation(
            @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(reservationId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "예약 취소 성공", null)
        );
    }
}
