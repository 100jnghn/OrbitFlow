package com.finalproj.orbitflow.attendance.repository;

import com.finalproj.orbitflow.attendance.entity.EmployeeAttRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeAttRuleRepository extends JpaRepository<EmployeeAttRule, Long> {

    // 특정 회사에 속한 모든 예외 규칙을 조회 (규칙 목록 조회)
    List<EmployeeAttRule> findAllByCompanyId(Long companyId);

}