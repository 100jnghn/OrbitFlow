package com.finalproj.orbitflow.attendance.leave.entity;


import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal; // DECIMAL(5, 2) 처리를 위해 사용

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveBalance
 * @since : 2026. 1. 8. 목요일
 */


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "leave_balance",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "employee_id", "year"})
        })
public class LeaveBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "total_granted", precision = 5, scale = 2)
    private BigDecimal totalGranted;

    @Column(name = "remaining_days", precision = 5, scale = 2)
    private BigDecimal remainingDays;

    public void updateBalance(BigDecimal days) {
        if (this.totalGranted == null) this.totalGranted = BigDecimal.ZERO;
        if (this.remainingDays == null) this.remainingDays = BigDecimal.ZERO;

        this.totalGranted = this.totalGranted.add(days);
        this.remainingDays = this.remainingDays.add(days);
    }

    public void updateBalanceFromActualUsage(BigDecimal actualUsedDays) {
        if (this.totalGranted == null) this.totalGranted = BigDecimal.ZERO;
        this.remainingDays = this.totalGranted.subtract(actualUsedDays);
    }

    public void deductBalance(BigDecimal deductedDays) {
        if (this.remainingDays == null) {
            this.remainingDays = BigDecimal.ZERO;
        }
        this.remainingDays = this.remainingDays.subtract(deductedDays);
    }

}
