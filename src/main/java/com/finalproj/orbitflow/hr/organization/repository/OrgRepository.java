package com.finalproj.orbitflow.hr.organization.repository;

import com.finalproj.orbitflow.hr.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrganizationRepository
 * @since : 2025-12-16 화요일
 */
public interface OrgRepository extends JpaRepository<Organization, Long> {

    /**
     * 회사별 전체 조직 조회 (트리 구성용)
     */
    List<Organization> findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(Long companyId);

    Optional<Organization> findByCompanyIdAndId(Long companyId, Long id);

    long countByCompanyIdAndParentOrgIdAndIsActiveTrue(
            Long companyId,
            Long parentOrgId
    );

    /**
     * 해당 카테고리를 사용하는 활성 조직 존재 여부
     */
    boolean existsByCompanyIdAndCategoryIdAndIsActiveTrue(Long companyId, Long categoryId);

}
