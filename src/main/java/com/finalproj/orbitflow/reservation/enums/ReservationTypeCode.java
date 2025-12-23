package com.finalproj.orbitflow.reservation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationTypeCode
 * @since : 2025-12-16 오후 1:08 화요일
 */
@Getter
@RequiredArgsConstructor
public enum ReservationTypeCode {
    MEETING("회의실"),
    CAR("차량"),
    ITEM("자원");

    private final String description;
}