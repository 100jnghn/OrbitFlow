package com.finalproj.orbitflow.global.time.calendar.init;

import com.finalproj.orbitflow.global.time.calendar.repository.CalendarDayRepository;
import com.finalproj.orbitflow.global.time.calendar.service.CalendarInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Year;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CalendarStartupInitializer
 * @since : 26. 1. 6. 화요일
 **/


@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarStartupInitializer implements ApplicationRunner {

    private final CalendarDayRepository calendarDayRepository;
    private final CalendarInitService calendarInitService;

    @Override
    public void run(ApplicationArguments args) {

        int year = LocalDate.now().getYear();

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        long count = calendarDayRepository.countByDateBetween(start, end);
        int expected = Year.isLeap(year) ? 366 : 365;

        if (count == expected) {
            return;
        }

        if (count > 0) {
            calendarInitService.regenerateYear(year);
        } else {
            calendarInitService.generateYear(year);
        }
    }
}
