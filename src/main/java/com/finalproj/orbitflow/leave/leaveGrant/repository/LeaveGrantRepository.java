package com.finalproj.orbitflow.leave.leaveGrant.repository;

import com.finalproj.orbitflow.leave.leaveGrant.entity.LeaveGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveGrantRepository
 * @since : 2025. 12. 24. 수요일
 */

@Repository
public interface LeaveGrantRepository extends JpaRepository<LeaveGrant,Long> {
    boolean existsByEmployeeIdAndGrantTypeAndGrantDate(Long id, String annualRegular, LocalDate today);

    List<LeaveGrant> findByCompanyIdAndEmployeeIdOrderByGrantDateDesc(Long companyId, Long employeeId);
}
