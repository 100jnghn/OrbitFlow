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

    ON_TIME("정상출근"), // 정시 또는 정시 이전에 출근
    LATE("지각"),      // 정시보다 늦게 출근
    ABSENT("결근"),    // 출근 기록이 없는 경우 (별도의 배치 처리 필요)
    VACATION("휴가"),
    BEFORE_WORK("근무예정");

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }
}