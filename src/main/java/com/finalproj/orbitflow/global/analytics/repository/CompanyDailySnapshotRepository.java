package com.finalproj.orbitflow.global.analytics.repository;

import com.finalproj.orbitflow.global.analytics.entity.CompanyDailySnapshot;
import com.finalproj.orbitflow.global.analytics.entity.CompanyDailySnapshotId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyDailySnapshotRepository extends JpaRepository<CompanyDailySnapshot, CompanyDailySnapshotId> {
}
