package com.finalproj.orbitflow.attendance.commute.repository;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.entity.EmployeeAttRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {


    Optional<Attendance> findByCompanyIdAndEmployeeIdAndWorkDate(Long companyId, Long employeeId, LocalDate today);
}

