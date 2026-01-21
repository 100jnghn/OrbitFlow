package com.finalproj.orbitflow.attendance.leave.entity;


import com.finalproj.orbitflow.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal; // DECIMAL(4, 2) 처리를 위해 사용
import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveGrant
 * @since : 2026. 1. 8. 목요일
 */


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
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "grant_date", nullable = false)
    private LocalDate grantDate;

    @Column(name = "granted_days", precision = 4, scale = 2)
    private BigDecimal grantedDays;

    @Column(name = "grant_type", length = 50)
    private String grantType;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired;

    public void updateExpiredStatus(Boolean isExpired) {
        this.isExpired = isExpired;
    }

}