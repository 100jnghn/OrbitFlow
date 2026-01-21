package com.finalproj.orbitflow.global.analytics.repository;

import com.finalproj.orbitflow.global.analytics.entity.CompanyDailySnapshot;
import com.finalproj.orbitflow.global.analytics.entity.CompanyDailySnapshotId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface CompanyDailySnapshotRepository extends JpaRepository<CompanyDailySnapshot, CompanyDailySnapshotId> {
    void deleteBySnapshotDate(LocalDate date);
}
