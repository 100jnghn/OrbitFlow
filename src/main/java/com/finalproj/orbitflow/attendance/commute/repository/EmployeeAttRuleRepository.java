package com.finalproj.orbitflow.attendance.commute.repository;

import com.finalproj.orbitflow.attendance.commute.entity.EmployeeAttRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeAttRuleRepository extends JpaRepository<EmployeeAttRule, Long> {

    // 특정 회사에 속한 모든 예외 규칙을 조회 (규칙 목록 조회)
    List<EmployeeAttRule> findAllByCompanyId(Long companyId);

    // [직원 본인용] 특정 직원의 현재 유효한 예외 규칙 조회
    // 1. 해당 사원에 속하고
    // 2. 오늘 날짜(CURRENT_DATE)가 validFrom과 validTo 사이에 있는 규칙 조회
//    List<EmployeeAttRule> findByEmployeeIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(
//            Long employeeId, LocalDate currentDate, LocalDate currentDate
//    );

}