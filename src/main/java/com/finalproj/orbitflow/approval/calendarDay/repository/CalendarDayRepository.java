package com.finalproj.orbitflow.approval.calendarDay.repository;

import com.finalproj.orbitflow.approval.calendarDay.entity.CalendarDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("""
                SELECT c
                FROM CalendarDay c
                WHERE c.isPublicHoliday = true
            """)
    List<CalendarDay> findAllByIsPublicHolidayTrue();
}