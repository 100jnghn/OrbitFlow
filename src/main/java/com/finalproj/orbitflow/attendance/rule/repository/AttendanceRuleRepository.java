package com.finalproj.orbitflow.attendance.rule.repository;

import com.finalproj.orbitflow.attendance.rule.entity.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceRuleRepository
 * @since : 2025. 12. 22. 월요일
 */


public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {

    Optional<AttendanceRule> findByCompanyIdAndIsDefaultTrue(Long companyId);

    boolean existsByCompanyId(Long id);
}