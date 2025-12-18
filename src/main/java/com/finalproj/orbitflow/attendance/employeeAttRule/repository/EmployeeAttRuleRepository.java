package com.finalproj.orbitflow.attendance.employeeAttRule.repository;

import com.finalproj.orbitflow.attendance.employeeAttRule.entity.EmployeeAttRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeAttRuleRepository extends JpaRepository<EmployeeAttRule, Long> {

    // [수정] 쿼리 메서드 명칭 분석 에러 방지를 위해 명시적 쿼리 사용
    @Query("SELECT r FROM EmployeeAttRule r WHERE r.employeeId = :employeeId " +
            "AND r.isActive = true AND :date BETWEEN r.validFrom AND r.validTo")
    Optional<EmployeeAttRule> findActiveRuleByEmployeeIdAndDate(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);


    List<EmployeeAttRule> findByCompanyId(Long currentCompanyId);
}