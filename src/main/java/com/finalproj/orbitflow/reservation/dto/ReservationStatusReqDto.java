package com.finalproj.orbitflow.reservation.dto;

import com.finalproj.orbitflow.reservation.enums.ReservationTypeCode;
import lombok.*;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationStatusReqDto
 * @since : 2025-12-22 오후 2:28 월요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationStatusReqDto {

    // MEETING // CAR // ITEM
    private ReservationTypeCode typeCode;

    // ITEM 예약일 때만 사용
    private Long itemCategoryId;

    private Long resourceId;

    private LocalDate reservationDate;
    private int startTime;
    private int endTime;

    private String reservationReason;
}
