package com.finalproj.orbitflow.schedule.repository;

import com.finalproj.orbitflow.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleRepository
 * @since : 2025-12-16 오후 1:17 화요일
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
