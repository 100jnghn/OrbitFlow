package com.finalproj.orbitflow.global.analytics.repository;

import com.finalproj.orbitflow.global.analytics.entity.CompanyDailyAiUsage;
import com.finalproj.orbitflow.global.analytics.entity.CompanyDailyAiUsageId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyDailyAiUsageRepository extends JpaRepository<CompanyDailyAiUsage, CompanyDailyAiUsageId> {
}
