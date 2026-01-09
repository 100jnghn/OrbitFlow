package com.finalproj.orbitflow.approval.attendanceEvent.repository;

import com.finalproj.orbitflow.approval.attendanceEvent.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AttendanceEventRepository
 * @since : 26. 1. 1. 목요일
 **/

public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {

    void deleteByEmployeeIdAndStartDateAfter(Long employeeId, LocalDate date);
}
