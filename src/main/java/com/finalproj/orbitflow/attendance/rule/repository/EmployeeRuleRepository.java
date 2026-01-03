package com.finalproj.orbitflow.attendance.rule.repository;

import com.finalproj.orbitflow.attendance.rule.entity.EmployeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeRuleRepository extends JpaRepository<EmployeeRule, Long> {


    @Query("SELECT r FROM EmployeeRule r " +
            "WHERE r.employeeId = :employeeId " +
            "AND r.isActive = true " +
            "AND r.validFrom <= :targetDate " +
            "AND (r.validTo IS NULL OR r.validTo >= :targetDate) " +
            "ORDER BY r.validFrom DESC")
    Optional<EmployeeRule> findActiveRuleByEmployeeIdAndDate(
            @Param("employeeId") Long employeeId,
            @Param("targetDate") LocalDate targetDate
    );


    List<EmployeeRule> findByCompanyIdAndIsActiveTrue(Long companyId);

    List<EmployeeRule> findByCompanyIdAndIsActiveTrueOrderByAppliedAtDesc(Long companyId);
}