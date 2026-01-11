package com.finalproj.orbitflow.attendance.leave.entity;


import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal; // DECIMAL(5, 2) 처리를 위해 사용

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "leave_balance",
        uniqueConstraints = {
                // 회사, 사원, 연도가 유일한 키가 될 가능성이 높음
                @UniqueConstraint(columnNames = {"company_id", "employee_id", "year"})
        })
public class LeaveBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;         // 잔여 ID (PK)

    @Column(name = "company_id", nullable = false)
    private Long companyId;         // 회사 ID (FK)

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;        // 사원 ID (FK)

    @Column(name = "year", nullable = false)
    private Integer year;           // 연차 기준 연도

    @Column(name = "total_granted", precision = 5, scale = 2)
    private BigDecimal totalGranted; // 총 부여 일수 (DECIMAL 5,2)

    @Column(name = "remaining_days", precision = 5, scale = 2)
    private BigDecimal remainingDays; // 잔여 일수 (DECIMAL 5,2)

    // LeaveBalance.java 내 수정
    public void updateBalance(BigDecimal days) {
        if (this.totalGranted == null) this.totalGranted = BigDecimal.ZERO;
        if (this.remainingDays == null) this.remainingDays = BigDecimal.ZERO;

        // 기존 잔합에 부여된 일수를 더함
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
