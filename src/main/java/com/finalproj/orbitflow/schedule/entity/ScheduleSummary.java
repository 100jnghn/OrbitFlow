package com.finalproj.orbitflow.schedule.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleSummary
 * @since : 2025-12-16 오후 1:18 화요일
 */
@Entity
@Table(name = "schedule_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScheduleSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "daily_summary", columnDefinition = "TEXT")
    private String dailySummary;

    @Column(name = "weekly_summary", columnDefinition = "TEXT")
    private String weeklySummary;

    public void update(String dailySummary, String weeklySummary) {
        this.dailySummary = dailySummary;
        this.weeklySummary = weeklySummary;

    }
}