package com.finalproj.orbitflow.attendance.employeeAttRule.repository;

import com.finalproj.orbitflow.attendance.employeeAttRule.entity.EmployeeAttRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeAttRuleRepository extends JpaRepository<EmployeeAttRule, Long> {

    @Query("SELECT r FROM EmployeeAttRule r " +
            "WHERE r.employeeId = :employeeId " +
            "AND r.isActive = true " +
            "AND :targetDate BETWEEN r.validFrom AND r.validTo")
    Optional<EmployeeAttRule> findActiveRuleByEmployeeIdAndDate(
            @Param("employeeId") Long employeeId,
            @Param("targetDate") LocalDate targetDate
    );


    List<EmployeeAttRule> findByCompanyId(Long currentCompanyId);
}