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

    Optional<Attendance> findByCompanyIdAndEmployeeIdAndWorkDate(Long companyId, Long employeeId, LocalDate workDate);

    /**
     * ✅ 에러 해결 포인트: findToday를 JPA 자동 생성 메서드가 아닌 default 메서드로 구현합니다.
     * JPA가 'today'라는 필드를 찾으려다 발생하는 QueryCreationException을 방지합니다.
     */
    default Optional<Attendance> findToday(Long companyId, Long employeeId) {
        return findByCompanyIdAndEmployeeIdAndWorkDate(companyId, employeeId, LocalDate.now());
    }

    // [사원용] 월별 페이징 조회 (최신순 정렬)
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :empId " +
            "AND a.workDate BETWEEN :start AND :end " +
            "AND (:status IS NULL OR a.status = :status) " +
            "ORDER BY a.workDate DESC")
    Page<Attendance> findHistoryWithPaging(
            @Param("empId") Long employeeId,
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate,
            @Param("status") AttendanceStatus status,
            Pageable pageable);


    /*
     * [관리자용] 전 사원 목록 + 기간 내 근태 기록
     */
    @Query("SELECT e, a FROM Employee e " +
            "LEFT JOIN Attendance a ON e.id = a.employeeId " +
            "AND a.workDate BETWEEN :startDate AND :endDate " +
            "WHERE e.company.id = :companyId " +
            "AND e.status = com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus.ACTIVE " +
            "AND (:keyword IS NULL OR e.name LIKE %:keyword% OR e.employeeNo LIKE %:keyword%) " +
            "AND (:status IS NULL OR a.status = :status OR (a.status IS NULL AND :status = 'ABSENT')) " +
            "ORDER BY a.workDate DESC, a.commuteAt DESC NULLS LAST") // ✅ 날짜 최신순, 출근시간 최신순 (기록 없는 사원은 뒤로)
    Page<Object[]> findAllEmployeesWithAttendance(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") AttendanceStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 상단 통계용: 특정 상태의 인원 수 카운트
    int countByCompanyIdAndWorkDateAndStatus(Long companyId, LocalDate workDate, AttendanceStatus status);

    // 상단 통계용: 퇴근 미처리 인원 수 카운트
    int countByCompanyIdAndWorkDateAndLeaveAtIsNull(Long companyId, LocalDate workDate);

    // 요약 정보용 리스트 조회
    List<Attendance> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(Long employeeId, LocalDate startDate, LocalDate endDate);

    // 스케줄러용: 특정 날짜의 기록 존재 여부 확인
    boolean existsByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
}