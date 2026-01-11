package com.finalproj.orbitflow.approval.attendanceEvent.repository;

import com.finalproj.orbitflow.approval.attendanceEvent.entity.AttendanceEvent;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

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

    void deleteByEmployeeIdAndStartDateAfter(Long employeeId, LocalDate date);
}
