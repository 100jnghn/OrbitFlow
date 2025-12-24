package com.finalproj.orbitflow.reservation.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationReqDto
 * @since : 2025-12-22 오후 2:27 월요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationReqDto {

    private String typeCode;
    private Long itemCategoryId;
    private Long resourceId;
    private LocalDate reservationDate;
    private LocalDate endDate;
    private Integer startTime;
    private Integer endTime;
    private String reservationReason;
}
