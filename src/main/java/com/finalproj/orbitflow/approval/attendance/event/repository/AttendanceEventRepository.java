package com.finalproj.orbitflow.approval.attendance.event.repository;

import com.finalproj.orbitflow.approval.attendance.event.entity.AttendanceEvent;
import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AttendanceEventRepository
 * @since : 26. 1. 1. 목요일
 **/


public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    boolean existsByEmployee_IdAndBaseRoleInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId,
            List<BaseRole> roles,
            LocalDate endDate,
            LocalDate startDate
    );


    @Query("""
        select e from AttendanceEvent e
        where e.employee.id = :employeeId
          and :today between e.startDate and coalesce(e.actualEndDate, e.endDate)
    """)
    Optional<AttendanceEvent> findActiveEvent(
            @Param("employeeId") Long employeeId,
            @Param("today") LocalDate today
    );
}
