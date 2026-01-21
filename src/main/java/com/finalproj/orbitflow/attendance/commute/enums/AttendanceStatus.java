package com.finalproj.orbitflow.attendance.commute.enums;

import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceStatus
 * @since : 2025. 12. 17. 수요일
 */


@Getter
public enum AttendanceStatus {

    ON_TIME("정상출근"),
    LATE("지각"),
    ABSENT("결근"),
    BEFORE_WORK("근무예정"),
    VACATION("휴가중"),
    OUTSIDE("외근중"),
    BUSINESS_TRIP("출장중");

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }
}