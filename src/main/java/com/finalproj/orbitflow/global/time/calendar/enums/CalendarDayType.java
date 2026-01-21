package com.finalproj.orbitflow.global.time.calendar.enums;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CalendarDayType
 * @since : 26. 1. 6. 화요일
 **/


public enum CalendarDayType {

    WORKDAY,
    PUBLIC_HOLIDAY,
    PAID_HOLIDAY,
    UNPAID_HOLIDAY;

    public boolean isChargeableForLeave() {
        return this == WORKDAY || this == UNPAID_HOLIDAY;
    }

    public boolean isHoliday() {
        return this == PUBLIC_HOLIDAY || this == PAID_HOLIDAY;
    }
}
