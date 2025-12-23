package com.finalproj.orbitflow.attendance.exception_rule.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "employee_att_rule")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeAttRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 아이디

    @Column(name = "company_id", nullable = false)
    private Long companyId; // 회사 아이디 (FK: company)

    @Column(name = "employee_id", nullable = false)
    private Long employeeId; // 직원 아이디 (FK: employee)

    @Column(name = "start_time")
    private LocalTime startTime; // 지정 출근 시간 (오버라이드)

    @Column(name = "end_time")
    private LocalTime endTime; // 지정 퇴근 시간 (오버라이드)

    @Column(name = "break_minutes")
    private Integer breakMinutes; // 지정 휴게 시간 (분)

    @Column(name = "reason", length = 255)
    private String reason; // 예외규칙 적용 사유

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom; // 규칙 적용 시작일 (DATE)

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo; // 규칙 적용 종료일 (DATE)

    @Column(name = "applied_at")
    private LocalDateTime appliedAt; // 적용된 시각 (DATETIME)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 규칙 활성화 상태


    public void updateRule(LocalTime startTime, LocalTime endTime, Integer breakMinutes, String reason, LocalDate validFrom, LocalDate validTo, Boolean isActive) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakMinutes = breakMinutes;
        this.reason = reason;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.isActive = isActive;
        // appliedAt은 서비스 레이어에서 update 시점을 기록하도록 처리
    }
}