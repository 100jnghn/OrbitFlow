package com.finalproj.orbitflow.reservation.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.reservation.dto.ReservationReqDto;
import com.finalproj.orbitflow.reservation.dto.ReservationResDto;
import com.finalproj.orbitflow.reservation.dto.ReservationStatusChangeReqDto;
import com.finalproj.orbitflow.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    // 내 예약 조회 (필터 처리)
    @GetMapping("/reservations/me")
    public ResponseEntity<ResponseDto> getMyReservations(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(defaultValue = "false") boolean showPast,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) String typeCode,
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "reservationDate", direction = Sort.Direction.ASC),
                    @SortDefault(sort = "startTime", direction = Sort.Direction.ASC)
            }) Pageable pageable
    ) {
        Page<ReservationResDto> result = reservationService.getMyReservations(
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

    // 예약 상세 조회
    @GetMapping("/reservations/me/{reservationId}")
    public ResponseEntity<ResponseDto> getMyReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        ReservationResDto reservation = reservationService.getMyReservation(reservationId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "예약 세부 조회 성공", reservation)
        );
    }

    // 해당 날짜 예약 조회
    @GetMapping("/reservations/date")
    public ResponseEntity<ResponseDto> getMyReservationByDate(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam String date,
            @RequestParam String typeCode
    ) {
        Long companyId = user.getCompanyId();

        List<ReservationResDto> reservations = reservationService.getReservationsByDate(
                companyId,
                date,
                typeCode
        );

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, date + "일 예약 조회 성공", reservations)
        );
    }

    // 내 예약 생성 (MEETING, CAR, ITEM)
    @PostMapping("/reservations/me")
    public ResponseEntity<ResponseDto> insertReservation(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody ReservationReqDto reservation
    ) {
        Long companyId = user.getCompanyId();
        Long userId = user.getEmployeeId();

        log.info("예약 신청: " + reservation.getTypeCode());

        reservationService.insertReservation(companyId, userId, reservation);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "예약 등록됨", null)
        );
    }

    // 내 예약 취소
    @PatchMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<ResponseDto> cancelReservation(
            @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(reservationId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "예약 취소 성공", null)
        );
    }


    // ----------------------------------------------------------------------- //
    // 관리자 예약 관리 기능 //

    // 관리자 - 예약 신청 리스트 조회
    @GetMapping("/admin/reservations")
    public ResponseEntity<ResponseDto> getReservations(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam(defaultValue = "false") boolean showPast,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) String typeCode,
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "reservationDate", direction = Sort.Direction.ASC),
                    @SortDefault(sort = "startTime", direction = Sort.Direction.ASC)
            }) Pageable pageable
    ) {
        Page<ReservationResDto> reservations = reservationService.getReservations(
                user.getCompanyId(),
                showPast,
                statusId,
                typeCode,
                pageable
        );

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "예약 리스트 조회 성공", reservations)
        );
    }

    // 관리자 - 예약 한 건 승인
    @PatchMapping("/admin/reservations/{reservationId}/approve")
    public ResponseEntity<ResponseDto> approveReservation(
            @PathVariable Long reservationId
    ) {
        reservationService.approveReservation(reservationId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "예약 승인 성공", null)
        );
    }

    // 관리자 - 예약 한 건 반려
    @PatchMapping("/admin/reservations/{reservationId}/reject")
    public ResponseEntity<ResponseDto> rejectReservation(
            @PathVariable Long reservationId,
            @RequestBody ReservationStatusChangeReqDto rejectReqDto
    ) {
        reservationService.rejectReservation(reservationId, rejectReqDto);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "예약 반려 처리 성공", null)
        );
    }

    // 관리자 - 예약 상태 변경
    @PatchMapping("/admin/reservations/{reservationId}/status")
    public ResponseEntity<ResponseDto> changeReservationStatus(
            @PathVariable Long reservationId,
            @RequestBody ReservationStatusChangeReqDto reservationStatusChangeReqDto
    ) {
        reservationService.changeReservationStatus(reservationId, reservationStatusChangeReqDto);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "예약 상태 변경 성공", null)
        );
    }

}
