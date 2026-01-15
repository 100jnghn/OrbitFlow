package com.finalproj.orbitflow.attendance.commute.repository;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommuteRepository extends JpaRepository<Attendance, Long> {

        // 기본 조회 메서드
        Optional<Attendance> findByCompanyIdAndEmployeeIdAndWorkDate(Long companyId, Long employeeId,
                        LocalDate workDate);

        // 오늘 근태 기록 조회
        default Optional<Attendance> findToday(Long companyId, Long employeeId) {
                return findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, LocalDate.now());
        }

        // [사원용] 월별 페이징 조회 (최신순)
        @Query("SELECT a FROM Attendance a WHERE a.employeeId = :employeeId " +
                        "AND a.workDate BETWEEN :startDate AND :endDate " +
                        "AND (:status IS NULL OR a.status = :status) " +
                        "ORDER BY a.workDate DESC")
        Page<Attendance> findHistoryWithPaging(
                        Long employeeId,
                        LocalDate startDate,
                        LocalDate endDate,
                        AttendanceStatus status,
                        Pageable pageable);

        // [관리자용] 전 사원 목록 + 기간 내 근태 기록 (WorkStatus 조건 포함, 필터링 누락 방지)
        @Query("""
        SELECT e, a
        FROM Employee e
        LEFT JOIN Attendance a
            ON e.id = a.employeeId
           AND a.workDate BETWEEN :startDate AND :endDate
        WHERE e.company.id = :companyId
          AND e.status = com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus.ACTIVE
          AND (
                :keyword IS NULL OR :keyword = ''
                OR e.name LIKE %:keyword%
                OR e.employeeNo LIKE %:keyword%
              )
          AND (
                :status IS NULL
                OR (a IS NOT NULL AND a.status = :status)
                OR (:targetWorkStatus IS NOT NULL AND e.workStatus = :targetWorkStatus)
                OR (:status = com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus.ABSENT
                    AND (a IS NULL OR a.commuteAt IS NULL)
                    AND (e.workStatus IS NULL
                         OR e.workStatus NOT IN (
                            com.finalproj.orbitflow.hr.employee.enums.WorkStatus.VACATION,
                            com.finalproj.orbitflow.hr.employee.enums.WorkStatus.OUTWORK,
                            com.finalproj.orbitflow.hr.employee.enums.WorkStatus.BUSINESS_TRIP
                         )
                    )
                )
              )
        ORDER BY COALESCE(a.workDate, :startDate) DESC, a.commuteAt DESC NULLS LAST
    """)
        Page<Object[]> findAllEmployeesWithAttendance(
                        Long companyId,
                        LocalDate startDate,
                        LocalDate endDate,
                        AttendanceStatus status,
                        com.finalproj.orbitflow.hr.employee.enums.WorkStatus targetWorkStatus,
                        String keyword,
                        Pageable pageable);

        // 특정 회사/날짜/상태 인원 수
        int countByCompanyIdAndWorkDateAndStatus(Long companyId, LocalDate workDate, AttendanceStatus status);

        // 특정 회사/날짜/퇴근 미처리 인원 수
        int countByCompanyIdAndWorkDateAndLeaveAtIsNull(Long companyId, LocalDate workDate);

        // 기간 내 기록 조회 (차트나 요약용)
        List<Attendance> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(Long employeeId, LocalDate startDate,
                        LocalDate endDate);

        // 스케줄러: 기록 존재 여부
        boolean existsByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
}