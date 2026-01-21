package com.finalproj.orbitflow.attendance.rule.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : EmployeeRule
 * @since : 2025. 12. 22. 월요일
 */


@Entity
@Table(name = "employee_att_rule")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "break_minutes")
    private Integer breakMinutes;

    @Column(length = 255)
    private String reason;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


    public void updateRule(LocalTime startTime, LocalTime endTime, Integer breakMinutes,
                           String reason, LocalDate validFrom, LocalDate validTo, Boolean isActive) {

        validateDateRange(validFrom, validTo);
        validateWorkTimes(startTime, endTime);
        validateBreakMinutes(breakMinutes);

        this.startTime = startTime;
        this.endTime = endTime;
        this.breakMinutes = breakMinutes;
        this.reason = reason;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.isActive = (isActive != null) ? isActive : this.isActive;
        this.appliedAt = LocalDateTime.now();
    }

    public void delete() {
        this.isActive = false;
        this.appliedAt = LocalDateTime.now();
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    private void validateWorkTimes(LocalTime start, LocalTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("퇴근 시간은 출근 시간보다 이후여야 합니다.");
        }
    }

    private void validateBreakMinutes(Integer minutes) {
        if (minutes != null && (minutes < 0 || minutes > 480)) {
            throw new IllegalArgumentException("휴게 시간은 0분에서 480분 사이여야 합니다.");
        }
    }
}