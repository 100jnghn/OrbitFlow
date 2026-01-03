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
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자 접근 제한
public class AttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(length = 100)
    private String name;

    @Column(name = "default_start_time")
    private LocalTime defaultStartTime;

    @Column(name = "default_end_time")
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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 규칙 업데이트 비즈니스 로직
    public void updateRule(LocalTime startTime, LocalTime endTime, Integer breakMinutes) {
        validateWorkTimes(startTime, endTime);

        this.defaultStartTime = startTime;
        this.defaultEndTime = endTime;
        this.defaultBreakMinutes = breakMinutes;
        this.updatedAt = LocalDateTime.now();
    }

    // 출퇴근 시간 유효성 검사
    private void validateWorkTimes(LocalTime start, LocalTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("퇴근 시간은 출근 시간보다 이후여야 합니다.");
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}