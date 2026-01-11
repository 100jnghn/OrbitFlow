package com.finalproj.orbitflow.attendance.leave.repository;

import com.finalproj.orbitflow.attendance.leave.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveBalanceRepository
 * @since : 2025. 12. 24. 수요일
 */

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    // 특정 사원의 특정 연도 잔합 정보를 조회합니다.
    // 테이블의 UNIQUE KEY (company_id, employee_id, year) 조건을 활용합니다.
    Optional<LeaveBalance> findByCompanyIdAndEmployeeIdAndYear(Long companyId, Long employeeId, int year);

    Optional<LeaveBalance> findTopByEmployeeIdOrderByYearDesc(Long employeeId);

    List<LeaveBalance> findByEmployeeId(Long employeeId);
}