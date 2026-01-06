package com.finalproj.orbitflow.approval.calendarDay.entity;

import com.finalproj.orbitflow.approval.calendarDay.enums.CalendarDayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class CalendarDay {

    @Id
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * 1 = Monday ~ 7 = Sunday
     */
    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20)
    private CalendarDayType dayType;

    @Column(name = "holiday_name", length = 50)
    private String holidayName;


    public boolean isHoliday() {
        return dayType.isHoliday();
    }

    public boolean isChargeableForLeave() {
        return dayType.isChargeableForLeave();
    }
}
