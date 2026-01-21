package com.finalproj.orbitflow.global.time.calendar.service;

import com.finalproj.orbitflow.global.time.calendar.entity.CalendarDay;
import com.finalproj.orbitflow.global.time.calendar.enums.CalendarDayType;
import com.finalproj.orbitflow.global.time.calendar.repository.CalendarDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CalendarInitService
 * @since : 26. 1. 6. 화요일
 **/


@Service
@RequiredArgsConstructor
public class CalendarInitService {

    private final CalendarDayRepository calendarDayRepository;
    private final HolidayProvider holidayProvider; // 공휴일 공급자

    @Transactional
    public void regenerateYear(int year) {

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        calendarDayRepository.deleteByDateBetween(start, end);
        generateYear(year);
    }

    @Transactional
    public void generateYear(int year) {

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        Map<LocalDate, String> publicHolidays =
                holidayProvider.getHolidays(year); // 공공 공휴일 only

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            if (calendarDayRepository.existsById(date)) continue;

            DayOfWeek dow = date.getDayOfWeek();

            CalendarDayType dayType = CalendarDayType.WORKDAY;
            String holidayName = null;

            /* 1️⃣ 근로자의 날 (고정 유급휴무일) */
            if (date.getMonthValue() == 5 && date.getDayOfMonth() == 1) {
                dayType = CalendarDayType.PAID_HOLIDAY;
                holidayName = "근로자의 날";
            }
            /* 2️⃣ 공공 공휴일 */
            else if (publicHolidays.containsKey(date)) {
                dayType = CalendarDayType.PUBLIC_HOLIDAY;
                holidayName = publicHolidays.get(date);
            }

            CalendarDay day = new CalendarDay(
                    date,
                    dow.getValue(),
                    dayType,
                    holidayName
            );

            calendarDayRepository.save(day);
        }
    }
}
