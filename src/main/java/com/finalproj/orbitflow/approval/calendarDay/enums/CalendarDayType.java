package com.finalproj.orbitflow.approval.calendarDay.enums;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CalendarDayType
 * @since : 26. 1. 6. 화요일
 **/


public enum CalendarDayType {

    WORKDAY,           // 정상 근무일
    PUBLIC_HOLIDAY,    // 공공 공휴일 (특일 API)
    PAID_HOLIDAY,      // 유급휴무일 (근로자의 날 등)
    UNPAID_HOLIDAY;    // 무급휴무일 (확장용)

    public boolean isHoliday() {
        return this != WORKDAY;
    }

    public boolean isChargeableForLeave() {
        return this == WORKDAY || this == UNPAID_HOLIDAY;
    }
}