package com.finalproj.orbitflow.approval.calendarDay.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : HolidayProvider
 * @since : 26. 1. 6. 화요일
 **/


public interface HolidayProvider {
    Map<LocalDate, String> getHolidays(int year);
}

