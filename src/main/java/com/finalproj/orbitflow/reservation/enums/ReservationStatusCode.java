package com.finalproj.orbitflow.reservation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ReservationStatusCode
 * @since : 2025-12-16 오후 1:06 화요일
 */
@Getter
@RequiredArgsConstructor
public enum ReservationStatusCode {
    PENDING("승인대기"),
    CONFIRM("예약완료"),
    REJECT("반려됨"),
    CANCELED("취소됨"),
    DELETED("삭제됨"),
    ETC("기타");

    private final String description;
}