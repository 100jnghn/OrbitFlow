package com.finalproj.orbitflow.attendance.attendanceDefaultRule.repository;

import com.finalproj.orbitflow.attendance.attendanceDefaultRule.entity.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {

    Optional<AttendanceRule> findByCompanyIdAndIsDefaultTrue(Long companyId);
}