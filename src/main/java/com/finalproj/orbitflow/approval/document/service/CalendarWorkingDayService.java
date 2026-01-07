package com.finalproj.orbitflow.approval.document.service;

import com.finalproj.orbitflow.approval.calendarDay.entity.CalendarDay;
import com.finalproj.orbitflow.approval.calendarDay.repository.CalendarDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CalendarWorkingDayService
 * @since : 26. 1. 6. 화요일
 **/


@Service
@RequiredArgsConstructor
public class CalendarWorkingDayService implements WorkingDayService {

    private final CalendarDayRepository calendarDayRepository;

    @Override
    public List<LocalDate> getWorkingDates(
            LocalDate start,
            LocalDate end
    ) {
        List<LocalDate> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            final LocalDate currentDate = date;

            CalendarDay day = calendarDayRepository
                    .findById(currentDate)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "CalendarDay not initialized: " + currentDate
                            ));

            if (day.isChargeableForLeave()) {
                result.add(date);
            }
        }

        return result;
    }
}
