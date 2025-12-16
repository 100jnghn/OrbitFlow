package com.finalproj.orbitflow.hr.organization.repository;

import com.finalproj.orbitflow.hr.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrganizationRepository
 * @since : 2025-12-16 화요일
 */
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /**
     * 회사별 최상위 조직 조회 (parent_org_id = null)
     */
    List<Organization> findByCompanyIdAndParentOrgIdIsNullAndIsActiveTrueOrderByOrderIndexAsc(Long companyId);

    /**
     * 특정 조직의 하위 조직 조회
     */
    List<Organization> findByCompanyIdAndParentOrgIdAndIsActiveTrueOrderByOrderIndexAsc(
            Long companyId,
            Long parentOrgId
    );

    /**
     * 회사별 전체 조직 조회 (트리 구성용)
     */
    List<Organization> findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(Long companyId);
}
