package com.finalproj.orbitflow.attendance.leave.leaveGrant.entity;


import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal; // DECIMAL(4, 2) 처리를 위해 사용
import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "grant_history")
public class LeaveGrant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;           // 부여 ID (PK)

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;        // 사원 ID (FK)

    @Column(name = "company_id", nullable = false)
    private Long companyId;         // 회사 ID (FK)

    @Column(name = "grant_date", nullable = false)
    private LocalDate grantDate;    // 부여 발효일 (DATE)

    @Column(name = "granted_days", precision = 4, scale = 2)
    private BigDecimal grantedDays; // 부여 일수 (DECIMAL 4,2)

    @Column(name = "grant_type", length = 50)
    private String grantType;       // 부여 유형

    @Column(name = "expiration_date")
    private LocalDate expirationDate; // 소멸 예정일 (DATE)

    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired;      // 소멸 처리 여부

    public void updateExpiredStatus(Boolean isExpired) {
        this.isExpired = isExpired;
    }

}