package com.finalproj.orbitflow.approval.calendarDay.service;

import com.finalproj.orbitflow.approval.calendarDay.entity.CalendarDay;
import com.finalproj.orbitflow.approval.calendarDay.repository.CalendarDayRepository;
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
    public void generateYear(int year) {

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        Map<LocalDate, String> holidays =
                holidayProvider.getHolidays(year); // date -> name

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            if (calendarDayRepository.existsById(date)) continue;

            DayOfWeek dow = date.getDayOfWeek();

            String holidayName = holidays.get(date);

            CalendarDay day = new CalendarDay(
                    date,
                    dow.getValue(),
                    holidayName != null,
                    holidayName
            );

            calendarDayRepository.save(day);
        }
    }
}