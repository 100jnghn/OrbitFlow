package com.finalproj.orbitflow.global.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CompanyDailySnapshotId implements Serializable {
    private LocalDate snapshotDate;
    private Long companyId;
}
