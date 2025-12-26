package com.finalproj.orbitflow.hr.organization.repository;

import com.finalproj.orbitflow.hr.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * 회사별 전체 조직 조회 (트리 구성용) - 활성만
     */
    List<Organization> findByCompanyIdAndIsActiveTrueOrderByParentOrgIdAscOrderIndexAsc(Long companyId);

    /**
     * 회사별 전체 조직 조회 - 비활성까지 포함
     */
    List<Organization> findByCompanyIdOrderByIsActiveDescParentOrgIdAscOrderIndexAsc(Long companyId);


    Optional<Organization> findByCompanyIdAndId(Long companyId, Long id);

    long countByCompanyIdAndParentOrgIdAndIsActiveTrue(
            Long companyId,
            Long parentOrgId
    );

    /**
     * 해당 카테고리를 사용하는 활성 조직 존재 여부 (OrgCategory 비활성 정책용)
     */
    boolean existsByCompanyIdAndCategoryIdAndIsActiveTrue(Long companyId, Long categoryId);

    /**
     * 하위 조직 존재 여부(활성)
     */
    boolean existsByCompanyIdAndParentOrgIdAndIsActiveTrue(Long companyId, Long parentOrgId);

    /**
     * 형제 단위 이름 중복(생성)
     */
    boolean existsByCompanyIdAndParentOrgIdAndNameAndIsActiveTrue(Long companyId, Long parentOrgId, String name);

    /**
     *  형제 단위 이름 중복(수정 - 자기 자신 제외)
    */
    boolean existsByCompanyIdAndParentOrgIdAndNameAndIsActiveTrueAndIdNot(
            Long companyId,
            Long parentOrgId,
            String name,
            Long id
    );

    /**
     * 특정 카테고리의 모든 조직 조회
     */
    List<Organization> findByCompanyIdAndCategoryIdAndIsActiveTrueOrderByOrderIndexAsc(Long companyId, Long categoryId);

    List<Organization> findOrganizationsById(Long id);


    @Query(
            value = """

                    WITH RECURSIVE org_hierarchy AS (
            SELECT
                id,
                category_id      AS categoryId,
                parent_org_id    AS parentOrgId,
                name,
                order_index      AS orderIndex,
                CASE WHEN is_active = 1 THEN true ELSE false END AS isActive
            FROM organization
            WHERE id = :orgId
              AND is_active = 1

            UNION ALL

            SELECT
                o.id,
                o.category_id,
                o.parent_org_id,
                o.name,
                o.order_index,
                CASE WHEN o.is_active = 1 THEN true ELSE false END
            FROM organization o
            JOIN org_hierarchy oh
              ON o.id = oh.parentOrgId
            WHERE o.is_active = 1
        )
        SELECT
            id,
            categoryId,
            parentOrgId,
            name,
            orderIndex,
            isActive
        FROM org_hierarchy
        """,
            nativeQuery = true
    )
    List<OrgResView> findHierarchy(@Param("orgId") Long orgId);


    @Query("""
    SELECT COALESCE(MAX(o.orderIndex), 0)
    FROM Organization o
    WHERE o.companyId = :companyId
      AND o.parentOrgId = :parentOrgId
      AND o.isActive = true
""")
    int findMaxOrderIndex(
            @Param("companyId") Long companyId,
            @Param("parentOrgId") Long parentOrgId
    );

}
