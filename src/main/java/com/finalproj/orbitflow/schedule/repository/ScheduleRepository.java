package com.finalproj.orbitflow.schedule.repository;

import com.finalproj.orbitflow.schedule.entity.Schedule;
import com.finalproj.orbitflow.schedule.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleRepository
 * @since : 2025-12-16 오후 1:17 화요일
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * 전사 일정 월별 전체 조회
     */
    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                  AND s.isCompany = true
                  AND s.startAt <= :endOfMonth
                  AND s.endAt >= :startOfMonth
                ORDER BY s.startAt ASC
            """)
    List<Schedule> findMonthlyCompanySchedules(
            @Param("companyId") Long companyId,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth
    );

    /**
     * 전사 일정 월별 상태 필터 조회
     */
    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                  AND s.isCompany = true
                  AND s.status = :status
                  AND s.startAt <= :endOfMonth
                  AND s.endAt >= :startOfMonth
                ORDER BY s.startAt ASC
            """)
    List<Schedule> findMonthlyCompanySchedulesByStatus(
            @Param("companyId") Long companyId,
            @Param("status") ScheduleStatus status,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth
    );

    int deleteByIdAndCompanyId(Long scheduleId, Long companyId);

    Schedule findByIdAndCompanyId(Long scheduleId, Long companyId);

    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.isCompany = false 
                    AND s.status = :status
                    AND s.orgId IN :orgIds
                    AND s.startAt <= :endOfMonth
                    AND s.endAt >= :startOfMonth
                ORDER BY s.startAt ASC
            """)
    List<Schedule> findMonthlyOrganizationSchedules(
            @Param("companyId") Long companyId,
            @Param("status") ScheduleStatus status,
            @Param("orgIds") List<Long> orgIds,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth
    );

    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.status = :scheduleStatus
                    AND s.employeeId = :employeeId
                    AND s.isCompany = false
                    AND s.orgCategoryId IS NULL
                    AND s.orgId IS NULL
                    AND s.startAt <= :endOfMonth
                    AND s.endAt >= :startOfMonth
                ORDER BY s.startAt ASC  
            """)
    List<Schedule> findMonthlyEmployeeSchedules(
            Long companyId,
            ScheduleStatus scheduleStatus,
            Long employeeId,
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth
    );

    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.isCompany = true
                    AND s.status = :status
                    AND s.startAt <= :endDay
                    AND s.endAt >= :today
                ORDER BY s.startAt ASC
            """)
    List<Schedule> findWeeklyCompanySchedules(
            Long companyId,
            ScheduleStatus scheduleStatus,
            LocalDateTime today,
            LocalDateTime endDay
    );

    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.isCompany = false
                    AND s.orgId IN :orgIds
                    AND s.status = :scheduleStatus
                    AND s.startAt <= :endDay
                    AND s.endAt >= :today
                ORDER BY s.startAt ASC
            """)
    List<Schedule> findWeeklyOrganizationSchedules(
            Long companyId,
            ScheduleStatus scheduleStatus,
            List<Long> orgIds,
            LocalDateTime today,
            LocalDateTime endDay
    );

    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.status = :scheduleStatus
                    AND s.employeeId = :employeeId
                    AND s.isCompany = false
                    AND s.orgCategoryId IS NULL
                    AND s.orgId IS NULL
                    AND s.startAt <= :endDay
                    AND s.endAt >= :today
                ORDER BY s.startAt ASC
            """)
    List<Schedule> findWeeklyEmployeeSchedules(
            Long companyId,
            ScheduleStatus scheduleStatus,
            Long employeeId,
            LocalDateTime today,
            LocalDateTime endDay
    );

    @Query("""
                    SELECT s
                    FROM Schedule s
                    WHERE s.companyId = :companyId
                        AND s.status = :scheduleStatus
                        AND s.isCompany = false
                        AND s.orgId IN :orgIds
                        AND s.startAt < :endOfDay
                        AND s.endAt > :startOfDay
            """)
    List<Schedule> findDateOrganizationSchedules(
            Long companyId,
            ScheduleStatus scheduleStatus,
            List<Long> orgIds,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.status = :scheduleStatus
                    AND s.isCompany = true
                    AND s.startAt < :endOfDay
                    AND s.endAt > :startOfDay
            """)
    List<Schedule> findDateCompanySchedules(
            Long companyId,
            ScheduleStatus scheduleStatus,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.status = :scheduleStatus
                    AND s.isCompany = false
                    AND s.employeeId = :employeeId
                    AND s.orgId IS NULL
                    AND s.orgCategoryId IS NULL
                    AND s.startAt < :endOfDay
                    AND s.endAt > :startOfDay
            """)
    List<Schedule> findDateEmployeeSchedules(
            Long companyId,
            ScheduleStatus scheduleStatus,
            Long employeeId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
