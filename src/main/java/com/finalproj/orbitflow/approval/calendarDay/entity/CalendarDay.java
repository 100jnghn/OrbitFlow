package com.finalproj.orbitflow.approval.calendarDay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CalendarDay
 * @since : 26. 1. 6. 화요일
 **/


@Entity
@Table(name = "calendar_day")
@Getter
@NoArgsConstructor
public class CalendarDay {

    @Id
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * 1 = Monday ~ 7 = Sunday
     */
    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "is_public_holiday", nullable = false)
    private boolean publicHoliday;

    @Column(name = "holiday_name", length = 50)
    private String holidayName;

    public CalendarDay(
            LocalDate date,
            int dayOfWeek,
            boolean publicHoliday,
            String holidayName
    ) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.publicHoliday = publicHoliday;
        this.holidayName = holidayName;
    }

    public boolean isHoliday() {
        return publicHoliday;
    }
}