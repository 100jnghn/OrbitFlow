package com.finalproj.orbitflow.hr.orgPositionUsage.repository;

import com.finalproj.orbitflow.hr.orgPositionUsage.entity.OrgPositionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgPositionUsageRepository
 * @since : 2025-12-16 화요일
 */
public interface OrgPositionUsageRepository extends JpaRepository<OrgPositionUsage, Long> {

    List<OrgPositionUsage> findByOrganization_IdAndIsEnabledTrue(Long orgId);
}