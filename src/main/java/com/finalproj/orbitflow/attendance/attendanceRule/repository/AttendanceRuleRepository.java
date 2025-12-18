package com.finalproj.orbitflow.attendance.attendanceRule.repository;

import com.finalproj.orbitflow.attendance.attendanceRule.entity.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {

    // 회사 ID와 기본 규칙 여부를 통해 유일한 기본 규칙을 조회
    Optional<AttendanceRule> findByCompanyIdAndIsDefaultTrue(Long companyId);
}