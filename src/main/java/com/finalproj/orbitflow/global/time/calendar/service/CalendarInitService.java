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
 * 연 단위 캘린더(CalendarDay) 초기화 및 재생성 서비스.
 *
 * <p>
 * 지정된 연도의 모든 날짜에 대해 {@link CalendarDay} 데이터를 생성하며,
 * 근무일, 공공 공휴일, 유급 휴무일 등의 기본 상태를 초기화한다.
 * </p>
 *
 * <p>
 * 공공 공휴일 정보는 외부 {@link HolidayProvider}를 통해 조회하여 반영하며,
 * 근로자의 날과 같은 고정 유급 휴무일은 별도의 규칙으로 적용한다.
 * </p>
 *
 * @author : Choi MinHyeok
 * @filename : CalendarInitService
 * @since : 26. 1. 6. 화요일
 **/


@Service
@RequiredArgsConstructor
public class CalendarInitService {

    private final CalendarDayRepository calendarDayRepository;
    private final HolidayProvider holidayProvider;


    @Transactional
    public void regenerateYear(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        calendarDayRepository.deleteByDateBetween(start, end);
        generateYear(year);
    }

    @Transactional
    public void generateYear(int year) {
        Map<LocalDate, String> publicHolidays =
                holidayProvider.getHolidays(year);

        generateBaseYear(year, publicHolidays);
        applyFixedPaidHolidays(year);
    }


    private void generateBaseYear(
            int year,
            Map<LocalDate, String> publicHolidays
    ) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            if (calendarDayRepository.existsById(date)) continue;

            DayOfWeek dow = date.getDayOfWeek();

            CalendarDayType dayType = publicHolidays.containsKey(date)
                    ? CalendarDayType.PUBLIC_HOLIDAY
                    : CalendarDayType.WORKDAY;

            calendarDayRepository.save(new CalendarDay(
                    date,
                    dow.getValue(),
                    dayType,
                    publicHolidays.get(date)
            ));
        }
    }

    private void applyFixedPaidHolidays(int year) {
        LocalDate laborDay = LocalDate.of(year, 5, 1);

        calendarDayRepository.findById(laborDay)
                .ifPresent(day ->
                        day.changeType(
                                CalendarDayType.PAID_HOLIDAY,
                                "근로자의 날"
                        )
                );
    }
}
