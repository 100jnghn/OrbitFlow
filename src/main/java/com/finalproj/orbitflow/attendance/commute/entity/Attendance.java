package com.finalproj.orbitflow.attendance.commute.entity;

import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import com.finalproj.orbitflow.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * * @author : hayeon
 * @filename : Attendance
 * @since : 2025. 12. 21. 일요일
 */


@Entity
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "work_date"})
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "commute_at")
    private LocalDateTime commuteAt;

    @Column(name = "leave_at")
    private LocalDateTime leaveAt;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private AttendanceStatus status;

    @Column(name = "applied_rule_id")
    private Long appliedRuleId;

    @Column(name = "is_corrected", nullable = false)
    private Boolean isCorrected = false;

    @Column(name = "correction_reason", length = 255)
    private String correctionReason;

    public void updateStatus(AttendanceStatus status, String correctionReason) {
        this.status = status;
        this.correctionReason = correctionReason;
        this.markAsCorrected();
    }

    public void markAsCorrected() {
        this.isCorrected = true;
    }


    public void updateTimeByAdmin(LocalDateTime commuteAt, LocalDateTime leaveAt) {

        this.isCorrected = true;

        if (commuteAt != null) {
            this.commuteAt = commuteAt;
        }
        if (leaveAt != null) {
            this.leaveAt = leaveAt;
        }
    }

    public void recordLeave() {
        if (this.leaveAt != null) {
            throw new BusinessException("이미 퇴근 처리되었습니다.");
        }
        this.leaveAt = LocalDateTime.now();
    }

    public void updateStatusAutomatically(AttendanceStatus status) {
        this.status = status;
    }





}