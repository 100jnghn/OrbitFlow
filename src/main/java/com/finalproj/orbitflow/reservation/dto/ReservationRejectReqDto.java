package com.finalproj.orbitflow.reservation.dto;

import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationRejectReqDto
 * @since : 2025-12-24 오후 10:48 수요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRejectReqDto {

    private String rejectReason;
}
