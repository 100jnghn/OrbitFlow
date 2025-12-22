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

    // [관리자] 전사 근태 목록 조회 (날짜 범위 및 상태 필터링)
    // 수정 포인트: 반환 타입을 Page<Attendance>로 변경하여 서비스의 map 로직과 연결
    @Query("SELECT a FROM Attendance a WHERE a.companyId = :companyId " +
            "AND (:start IS NULL OR a.workDate >= CAST(:start AS localdate)) " +
            "AND (:end IS NULL OR a.workDate <= CAST(:end AS localdate)) " +
            "AND (:status IS NULL OR CAST(a.status AS string) = :status)")
    Page<Attendance> findAllByCompanyIdAndFilters(
            @Param("companyId") Long companyId,
            @Param("start") String start,
            @Param("end") String end,
            @Param("status") String status,
            Pageable pageable);

    // [관리자] 특정 상태 인원 카운트 (상단 요약용)
    int countByCompanyIdAndWorkDateAndStatus(Long companyId, LocalDate workDate, AttendanceStatus status);

    // [관리자] 퇴근 미처리 인원 카운트
    int countByCompanyIdAndWorkDateAndLeaveAtIsNull(Long companyId, LocalDate workDate);
}