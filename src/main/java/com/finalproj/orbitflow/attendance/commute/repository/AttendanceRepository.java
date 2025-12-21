package com.finalproj.orbitflow.attendance.commute.repository;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // 회사 ID와 사원 ID, 날짜로 조회
    Optional<Attendance> findByCompanyIdAndEmployeeIdAndWorkDate(Long companyId, Long employeeId, LocalDate workDate);

    // 오늘 기록 조회 편의 메서드
    default Optional<Attendance> findToday(Long companyId, Long employeeId) {
        return findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, LocalDate.now());
    }


    List<Attendance> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 서버 사이드 페이징 + 필터링 (상태값이 ALL이 아닐 때 조건 적용)
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :empId " +
            "AND a.workDate BETWEEN :start AND :end " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<Attendance> findHistoryWithPaging(
            @Param("empId") Long employeeId,
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate,
            @Param("status") AttendanceStatus status,
            Pageable pageable);

    boolean existsByEmployeeIdAndWorkDate(Long id, LocalDate yesterday);
}