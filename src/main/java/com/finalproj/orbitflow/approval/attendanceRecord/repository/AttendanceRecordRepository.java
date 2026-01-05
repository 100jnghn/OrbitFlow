package com.finalproj.orbitflow.approval.attendanceRecord.repository;

import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import com.finalproj.orbitflow.approval.document.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveRecordRepository
 * @since : 2025. 12. 27. 토요일
 */

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByCompanyIdAndEmployeeId(Long companyId, Long employeeId);

    @Query("SELECT ar FROM AttendanceRecord ar " +
            "WHERE ar.company.id = :companyId " +
            "AND ar.employee.id = :employeeId " +
            "AND (:status IS NULL OR ar.status = :status) " +
            "AND (:typeName IS NULL OR ar.leaveType.typeName = :typeName) " +
            "AND (:startDate IS NULL OR ar.startDate >= :startDate) " +
            "AND (:endDate IS NULL OR ar.startDate <= :endDate) " +
            "ORDER BY ar.startDate DESC")
    Page<AttendanceRecord> findAllLeaveHistoryWithFilters(
            @Param("companyId") Long companyId,
            @Param("employeeId") Long employeeId,
            @Param("typeName") String typeName,
            @Param("status") DocumentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);


    @Query("SELECT r FROM AttendanceRecord r " +
            "WHERE r.company.id = :companyId " +
            "AND r.employee.id = :employeeId " +
            "AND r.leaveType.isCountable = true " +
            "AND r.status = com.finalproj.orbitflow.approval.document.enums.DocumentStatus.APPROVED " +
            "AND FUNCTION('YEAR', r.startDate) = :year " +
            "AND (:typeName IS NULL OR r.leaveType.typeName = :typeName) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:startDate IS NULL OR r.startDate >= :startDate) " +
            "AND (:endDate IS NULL OR r.startDate <= :endDate) " +
            "ORDER BY r.startDate DESC")
    Page<AttendanceRecord> findUsageHistoryWithFilters(
            @Param("companyId") Long companyId,
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("typeName") String typeName,
            @Param("status") DocumentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}