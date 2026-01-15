package com.finalproj.orbitflow.attendance.leave.repository;

import com.finalproj.orbitflow.attendance.leave.entity.LeaveGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
public interface LeaveGrantRepository extends JpaRepository<LeaveGrant, Long> {

    boolean existsByEmployeeIdAndGrantTypeAndGrantDate(Long employeeId, String grantType, LocalDate grantDate);

    List<LeaveGrant> findByCompanyIdAndEmployeeIdOrderByGrantDateDesc(Long companyId, Long employeeId);

    List<LeaveGrant> findByExpirationDateBeforeAndIsExpiredFalse(LocalDate today);

    /**
     * 특정 연도에 해당 사원에게 ANNUAL_ 계열 연차가 이미 부여되었는지 확인
     */
    @Query("SELECT COUNT(lg) > 0 FROM LeaveGrant lg WHERE lg.employeeId = :empId " +
            "AND lg.grantType LIKE 'ANNUAL_%' " +
            "AND FUNCTION('YEAR', lg.grantDate) = :year")
    boolean existsAnnualLeaveForYear(@Param("empId") Long empId, @Param("year") int year);
}