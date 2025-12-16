package com.finalproj.orbitflow.attendance.commute.repository;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    /**
     * 특정 사원의 특정 날짜 출퇴근 기록 조회
     */
    Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
    
    /**
     * 특정 사원의 오늘 출퇴근 기록 조회
     */
    default Optional<Attendance> findTodayByEmployeeId(Long employeeId) {
        return findByEmployeeIdAndWorkDate(employeeId, LocalDate.now());
    }
}

