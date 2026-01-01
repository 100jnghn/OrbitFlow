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
                    AND s.isPersonal = false 
                    AND s.orgCategoryId IS NOT NULL
                    AND s.orgId IS NULL
                    AND s.startAt <= :endOfMonth
                    AND s.endAt >= :startOfMonth
                ORDER BY s.startAt ASC
            """)
    List<Schedule> findCompanySchedules(
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
                    AND s.isPersonal = false
                    AND s.status = :status
                    AND s.orgCategoryId IS NOT NULL
                    AND s.orgId IS NULL
                    AND s.startAt <= :endOfMonth
                    AND s.endAt >= :startOfMonth
                ORDER BY s.startAt ASC
            """)
    List<Schedule> findCompanySchedulesByStatus(
            @Param("companyId") Long companyId,
            @Param("status") ScheduleStatus status,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth
    );

    /**
     * 사용자 전사 일정 조회
     */
    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.isCompany = true
                    AND s.isPersonal = false
                    AND s.orgCategoryId IS NOT NULL
                    AND s.orgId IS NULL 
                    AND s.status = :scheduleStatus
                    AND s.startAt <= :endOfDate
                    AND s.endAt >= :startOfDate
                ORDER BY s.startAt ASC
            """)
    List<Schedule> findUserCompanySchedules(
            Long companyId,
            LocalDateTime startOfDate,
            LocalDateTime endOfDate,
            ScheduleStatus scheduleStatus
    );

    /**
     * 일정 삭제 (Hard Delete)
     */
    int deleteByIdAndCompanyId(Long scheduleId, Long companyId);

    /**
     * 일정 상세 조회
     */
    Schedule findByIdAndCompanyId(Long scheduleId, Long companyId);

    /**
     * 조직 일정 검색
     */
    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.isCompany = false
                    AND s.isPersonal = false
                    AND s.orgCategoryId IS NOT NULL
                    AND s.orgId IN :orgIds
                    AND s.status = :scheduleStatus
                    AND s.startAt <= :endOfDate
                    AND s.endAt >= :startOfDate
            """)
    List<Schedule> findOrganizationSchedules(
            Long companyId,
            ScheduleStatus scheduleStatus,
            List<Long> orgIds,
            LocalDateTime startOfDate,
            LocalDateTime endOfDate
    );

    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.isCompany = false
                    AND s.isPersonal = false
                    AND s.orgCategoryId IS NOT NULL
                    AND s.orgId IN :orgIds
                    AND s.status = :scheduleStatus
                    AND s.startAt < :endOfDay
                    AND s.endAt > :startOfDay
                ORDER BY s.startAt ASC
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
                    AND s.isPersonal = false
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
                    AND s.isPersonal = true
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

    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.status = :scheduleStatus
                    AND s.isCompany = true
                    AND s.isPersonal = true
                    AND s.employeeId = :employeeId
                    AND s.orgId IS NOT NULL
                    AND s.orgCategoryId IS NULL
                    AND s.startAt < :endOfDay
                    AND s.endAt > :startOfDay
            """)
    List<Schedule> findDateCompanyEmployeeSchedules(
            Long companyId,
            ScheduleStatus scheduleStatus,
            Long employeeId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );


    /**
     * 진짜 개인 일정 조회
     */
    @Query("""
                SELECT s
                FROM Schedule s
                WHERE s.companyId = :companyId
                    AND s.status = :scheduleStatus
                    AND s.isCompany = false
                    AND s.isPersonal = true
                    AND s.employeeId = :employeeId
                    AND s.orgId IS NULL
                    AND s.orgCategoryId IS NULL
                    AND s.startAt < :endOfDate
                    AND s.endAt > :startOfDate
            """)
    List<Schedule> findEmployeeSchedules(
            Long companyId,
            ScheduleStatus scheduleStatus,
            Long employeeId,
            LocalDateTime startOfDate,
            LocalDateTime endOfDate
    );

    @Query("""
                    SELECT s
                    FROM Schedule s
                    WHERE s.companyId = :companyId
                        AND s.status = :scheduleStatus
                        AND s.isCompany = true
                        AND s.isPersonal = true
                        AND s.employeeId = :employeeId
                        AND s.orgId IS NOT NULL
                        AND s.orgCategoryId IS NULL
                        AND s.startAt < :endOfDate
                        AND s.endAt > :startOfDate
            """)
    List<Schedule> findCompanyEmployeeSchedules(
            Long companyId,
            Long employeeId,
            LocalDateTime startOfDate,
            LocalDateTime endOfDate,
            ScheduleStatus scheduleStatus
    );

}
