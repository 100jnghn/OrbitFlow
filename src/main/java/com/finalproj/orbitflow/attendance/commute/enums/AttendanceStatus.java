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

    ON_TIME("정상출근"),    // 정시 또는 정시 이전에 출근
    LATE("지각"),         // 정시보다 늦게 출근
    ABSENT("결근"),       // 출근 시간이 지났음에도 기록이 없는 상태
    BEFORE_WORK("근무예정"), // 오늘 근무 대상자이나 아직 출근 전인 상태
    VACATION("휴가중"),    // 승인된 휴가 상태
    OUTSIDE("외근중"),     // 외부 근무 상태
    BUSINESS_TRIP("출장중"); // 출장 상태

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }
}