package com.finalproj.orbitflow.attendance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_rule")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id", nullable = false)
    private Long ruleId; // 규칙 아이디

    @Column(name = "company_id", nullable = false)
    private Long companyId; // 회사 아이디 (FK: company)

    @Column(name = "name", length = 100)
    private String name; // 규칙 명칭

    @Column(name = "default_start_time", nullable = false)
    private LocalTime defaultStartTime; // 기본 출근 기준 시간 (TIME)

    @Column(name = "default_end_time", nullable = false)
    private LocalTime defaultEndTime; // 기본 퇴근 기준 시간 (TIME)

    @Column(name = "default_break_minutes")
    private Integer defaultBreakMinutes; // 기본 휴게 시간 (분 단위)

    @Column(name = "late_threshold_min")
    private Integer lateThresholdMin; // 지각 판정 허용 시간 (분)

    @Column(name = "early_leave_threshold_min")
    private Integer earlyLeaveThresholdMin; // 조퇴 판정 허용 시간 (분)

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = true; // 회사의 기본 주 규칙 여부

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일시

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정일시

    public void updateRule(LocalTime startTime, LocalTime endTime, Integer breakMinutes) {
        this.defaultStartTime = startTime;
        this.defaultEndTime = endTime;
        this.defaultBreakMinutes = breakMinutes;
    }
}