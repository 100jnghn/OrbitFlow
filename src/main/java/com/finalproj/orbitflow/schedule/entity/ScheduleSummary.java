package com.finalproj.orbitflow.schedule.entity;

import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
public class ScheduleSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // MySQL의 TEXT 타입 매핑
    @Column(name = "week_summary", columnDefinition = "TEXT")
    private String weekSummary;

    @Column(name = "month_summary", columnDefinition = "TEXT")
    private String monthSummary;

    // BaseEntity를 상속받지 않고 createdAt만 사용
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}