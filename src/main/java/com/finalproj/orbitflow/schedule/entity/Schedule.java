package com.finalproj.orbitflow.schedule.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.schedule.enums.ScheduleStatus;
import com.finalproj.orbitflow.schedule.enums.ScheduleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : Schedule
 * @since : 2025-12-16 오후 1:12 화요일
 */
@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // 조직 카테고리 (부서 일정일 경우 사용, Nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private OrgCategory orgCategory;

    // 특정 부서 (부서 일정일 경우 사용, Nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organization organization;

    // 일정 유형 (COMPANY: 전사, PERSONAL: 개인 등)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ScheduleType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "schedule_title", nullable = false, length = 100)
    private String title;

    @Column(name = "schedule_description", length = 255)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // 시간은 정수형(예: 900 -> 09:00, 1430 -> 14:30)으로 관리
    @Column(name = "start_time", nullable = false)
    private Integer startTime;

    @Column(name = "end_time", nullable = false)
    private Integer endTime;

    // 일정 상태 (RELEASE: 공개, DELETED: 삭제됨, PRIVATE: 비공개 등)
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_status", nullable = false, length = 20)
    private ScheduleStatus status;


    public void update(String title, String description, LocalDate startDate, LocalDate endDate,
                       Integer startTime, Integer endTime, ScheduleStatus status) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
}