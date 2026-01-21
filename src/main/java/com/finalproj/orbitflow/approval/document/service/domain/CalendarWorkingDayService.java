package com.finalproj.orbitflow.approval.document.service.domain;

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

        if (start == null || end == null || end.isBefore(start)) {
            return result; // 잘못된 입력 → 빈 리스트
        }

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            CalendarDay day = calendarDayRepository.findById(date).orElse(null);

            if (day == null) {
                continue;
            }

            if (day.isChargeableForLeave()) {
                result.add(date);
            }
        }
        return result;
    }
}
