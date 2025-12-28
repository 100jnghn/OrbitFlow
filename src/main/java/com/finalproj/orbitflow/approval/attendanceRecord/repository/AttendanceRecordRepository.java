package com.finalproj.orbitflow.approval.attendanceRecord.repository;

import com.finalproj.orbitflow.approval.attendanceRecord.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
