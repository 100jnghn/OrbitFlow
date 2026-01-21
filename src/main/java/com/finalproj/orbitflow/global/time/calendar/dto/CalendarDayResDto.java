package com.finalproj.orbitflow.global.time.calendar.dto;

import com.finalproj.orbitflow.global.time.calendar.entity.CalendarDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : CalendarDayResDto
 * @since : 2026-01-06 오후 10:51 화요일
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarDayResDto {

    private LocalDate date;
    private int dayOfWeek;
    private String dayType;
    private String holidayName;

    public static CalendarDayResDto from(CalendarDay entity) {
        if (entity == null) {
            return null;
        }

        return CalendarDayResDto.builder()
                .date(entity.getDate())
                .dayOfWeek(entity.getDayOfWeek())
                .dayType(entity.getDayType().name())
                .holidayName(entity.getHolidayName())
                .build();
    }
}
