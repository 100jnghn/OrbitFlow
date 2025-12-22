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

    // 월별 근태 이력 조회 (요약용)
    List<Attendance> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    // [사원용] 서버 사이드 페이징 + 필터링
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :empId " +
            "AND a.workDate BETWEEN :start AND :end " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<Attendance> findHistoryWithPaging(
            @Param("empId") Long employeeId,
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate,
            @Param("status") AttendanceStatus status,
            Pageable pageable);

    // 스케줄러용 기록 존재 여부 확인
    boolean existsByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    /**
     * [핵심] 전 사원 목록을 불러오면서 특정 날짜의 근태 기록을 Left Join
     */
    @Query("SELECT e, a FROM Employee e " +
            "LEFT JOIN Attendance a ON e.id = a.employeeId AND a.workDate = :targetDate " +
            "WHERE e.company.id = :companyId " +
            "AND e.status = com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus.ACTIVE " +
            "AND (:status IS NULL OR a.status = :status OR (a.status IS NULL AND :status = 'ABSENT'))")
    Page<Object[]> findAllEmployeesWithAttendance(
            @Param("companyId") Long companyId,
            @Param("targetDate") LocalDate targetDate,
            @Param("status") AttendanceStatus status,
            Pageable pageable);

    // 상단 통계용
    int countByCompanyIdAndWorkDateAndStatus(Long companyId, LocalDate workDate, AttendanceStatus status);

    // 퇴근 미처리 카운트
    int countByCompanyIdAndWorkDateAndLeaveAtIsNull(Long companyId, LocalDate workDate);
}