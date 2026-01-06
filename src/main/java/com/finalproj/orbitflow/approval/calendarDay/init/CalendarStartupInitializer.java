package com.finalproj.orbitflow.approval.calendarDay.init;

import com.finalproj.orbitflow.approval.calendarDay.repository.CalendarDayRepository;
import com.finalproj.orbitflow.approval.calendarDay.service.CalendarInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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

        if (calendarDayRepository.count() > 0) {
            log.info("[CalendarInit] Calendar data already exists. Skip initialization.");
            return;
        }

        int currentYear = LocalDate.now().getYear();
        int nextYear = currentYear + 1;

        log.info("[CalendarInit] No calendar data found. Initializing {} and {}", currentYear, nextYear);

        calendarInitService.generateYear(currentYear);
        //calendarInitService.generateYear(nextYear);
    }
}