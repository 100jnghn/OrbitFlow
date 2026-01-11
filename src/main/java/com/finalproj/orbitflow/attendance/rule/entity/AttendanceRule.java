package com.finalproj.orbitflow.attendance.rule.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_rule")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(length = 100)
    private String name;

    @Column(name = "default_start_time", nullable = false)
    private LocalTime defaultStartTime;

    @Column(name = "default_end_time", nullable = false)
    private LocalTime defaultEndTime;

    @Column(name = "default_break_minutes")
    private Integer defaultBreakMinutes;

    @Column(name = "late_threshold_min")
    private Integer lateThresholdMin;

    @Column(name = "early_leave_threshold_min")
    private Integer earlyLeaveThresholdMin;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = true;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public void updateRule(LocalTime startTime, LocalTime endTime, Integer lateThresholdMin,Integer breakMinutes) {
        validateWorkTimes(startTime, endTime);

        this.defaultStartTime = startTime != null ? startTime : this.defaultStartTime;
        this.defaultEndTime = endTime != null ? endTime : this.defaultEndTime;
        this.lateThresholdMin = lateThresholdMin != null ? lateThresholdMin : this.lateThresholdMin;
        this.defaultBreakMinutes = breakMinutes != null ? breakMinutes : this.defaultBreakMinutes;

        this.updatedAt = LocalDateTime.now();
    }

    private void validateWorkTimes(LocalTime start, LocalTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("퇴근 시간은 출근 시간보다 이후여야 합니다.");
        }
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }


    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}