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

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveRecordRepository
 * @since : 2025. 12. 27. 토요일
 */

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord,Long> {
    List<AttendanceRecord> findByCompanyIdAndEmployeeIdOrderByStartDateDesc(Long companyId, Long employeeId);
    Page<AttendanceRecord> findByCompanyIdAndEmployeeIdOrderByStartDateDesc(Long companyId, Long employeeId, Pageable pageable);
    
    // 연차 차감 항목만 조회 (isCountable = true)
    @Query("SELECT ar FROM AttendanceRecord ar " +
           "WHERE ar.company.id = :companyId " +
           "AND ar.employee.id = :employeeId " +
           "AND ar.leaveType.isCountable = true " +
           "ORDER BY ar.startDate DESC")
    Page<AttendanceRecord> findAnnualLeaveHistory(
            @Param("companyId") Long companyId,
            @Param("employeeId") Long employeeId,
            Pageable pageable
    );
    
    // 연차 차감 항목 조회 (필터링 지원)
    @Query("SELECT ar FROM AttendanceRecord ar " +
           "WHERE ar.company.id = :companyId " +
           "AND ar.employee.id = :employeeId " +
           "AND ar.leaveType.isCountable = true " +
           "AND (COALESCE(:typeName, '') = '' OR ar.leaveType.typeName = :typeName) " +
           "AND (:status IS NULL OR ar.status = :status) " +
           "AND (:startDate IS NULL OR ar.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR ar.startDate <= :endDate) " +
           "ORDER BY ar.startDate DESC")
    Page<AttendanceRecord> findAnnualLeaveHistoryWithFilters(
            @Param("companyId") Long companyId,
            @Param("employeeId") Long employeeId,
            @Param("typeName") String typeName,
            @Param("status") DocumentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    
    // 모든 휴가 이력 조회 (필터링 지원)
    @Query("SELECT ar FROM AttendanceRecord ar " +
           "WHERE ar.company.id = :companyId " +
           "AND ar.employee.id = :employeeId " +
           "AND (COALESCE(:typeName, '') = '' OR ar.leaveType.typeName = :typeName) " +
           "AND (:status IS NULL OR ar.status = :status) " +
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
            Pageable pageable
    );
}
