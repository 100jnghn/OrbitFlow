package com.finalproj.orbitflow.global.time.calendar.service;

import com.finalproj.orbitflow.global.time.calendar.dto.CalendarDayResDto;
import com.finalproj.orbitflow.global.time.calendar.entity.CalendarDay;
import com.finalproj.orbitflow.global.time.calendar.enums.CalendarDayType;
import com.finalproj.orbitflow.global.time.calendar.repository.CalendarDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : CalendarDayService
 * @since : 2026-01-06 오후 10:44 화요일
 */
@Service
@RequiredArgsConstructor
public class CalendarDayService {

    private final CalendarDayRepository calendarDayRepository;

    // 공휴일 + 주말 반환
    @Transactional(readOnly = true)
    public List<CalendarDayResDto> getHolidays() {

        List<CalendarDay> list = calendarDayRepository.findByDayTypeNot(CalendarDayType.WORKDAY);

        return list.stream().map(CalendarDayResDto::from).toList();
    }
}
