package com.finalproj.orbitflow.approval.calendarDay.repository;

import com.finalproj.orbitflow.approval.calendarDay.entity.CalendarDay;
import com.finalproj.orbitflow.approval.calendarDay.enums.CalendarDayType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : CalendarDayRepository
 * @since : 26. 1. 6. 화요일
 **/


public interface CalendarDayRepository extends JpaRepository<CalendarDay, LocalDate> {


    void deleteByDateBetween(LocalDate start, LocalDate end);

    long countByDateBetween(LocalDate start, LocalDate end);

    List<CalendarDay> findByDayTypeNot(CalendarDayType calendarDayType);
}