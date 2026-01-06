package com.finalproj.orbitflow.approval.calendarDay.repository;

import com.finalproj.orbitflow.approval.calendarDay.entity.CalendarDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CalendarDayRepository
 * @since : 26. 1. 6. 화요일
 **/


public interface CalendarDayRepository extends JpaRepository<CalendarDay, LocalDate> {

    boolean existsByDateAndPublicHolidayTrue(LocalDate date);
}