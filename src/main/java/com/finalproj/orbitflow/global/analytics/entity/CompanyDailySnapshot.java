package com.finalproj.orbitflow.global.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "company_daily_snapshot")
@IdClass(CompanyDailySnapshotId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompanyDailySnapshot {

    @Id
    @Column(name = "snapshot_date")
    private LocalDate snapshotDate;

    @Id
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "employee_count", nullable = false)
    private Integer employeeCount;

    @Column(name = "file_count", nullable = false)
    private Integer fileCount;

    @Column(name = "file_bytes", nullable = false)
    private Long fileBytes;

    @Column(name = "active_yn", nullable = false, length = 1)
    private String activeYn;
}
