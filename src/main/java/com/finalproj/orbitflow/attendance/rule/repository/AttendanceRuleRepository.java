package com.finalproj.orbitflow.attendance.rule.repository;

import com.finalproj.orbitflow.attendance.rule.entity.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {

    Optional<AttendanceRule> findByCompanyIdAndIsDefaultTrue(Long companyId);

    boolean existsByCompanyId(Long id);
}