package com.finalproj.orbitflow.attendance.attendanceRule.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_rule")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 규칙 아이디

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

    public void updateDefaultRule(LocalTime startTime, LocalTime endTime, Integer breakMinutes) {
        this.defaultStartTime = startTime;
        this.defaultEndTime = endTime;
        this.defaultBreakMinutes = breakMinutes;
    }

    public void updateRule(LocalTime startTime, LocalTime endTime, Integer breakMinutes) {
        // 1. 유효성 검사 (선택 사항: 퇴근 시간이 출근 시간보다 빨라선 안 됨)
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("퇴근 시간은 출근 시간보다 이후여야 합니다.");
        }

        // 2. 새로운 값이 넘어온 경우에만 필드 업데이트 (Null-Safe 처리)
        if (startTime != null) {
            this.defaultStartTime = startTime;
        }

        if (endTime != null) {
            this.defaultEndTime = endTime;
        }

        if (breakMinutes != null) {
            this.defaultBreakMinutes = breakMinutes;
        }
    }



}