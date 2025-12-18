package com.finalproj.orbitflow.attendance.commute.repository;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // 특정 날짜 조회
    Optional<Attendance> findByCompanyIdAndEmployeeIdAndWorkDate(Long companyId, Long employeeId, LocalDate workDate);

    // 오늘 기록 조회 편의 메서드
    default Optional<Attendance> findToday(Long companyId, Long employeeId) {
        return findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, LocalDate.now());
    }
}
