package com.finalproj.orbitflow.approval.calendarDay.scheduler;

import com.finalproj.orbitflow.approval.calendarDay.service.CalendarInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CalendarYearScheduler
 * @since : 26. 1. 6. 화요일
 **/


@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarYearScheduler {

    private final CalendarInitService calendarInitService;

    /**
     * 매년 12월 31일 00:10 실행
     */
    @Scheduled(cron = "0 10 0 31 12 ?")
    public void generateNextYearCalendar() {

        int nextYear = LocalDate.now().plusYears(1).getYear();

        log.info("[CalendarScheduler] Generate calendar for year {}", nextYear);

        calendarInitService.generateYear(nextYear);
    }
}