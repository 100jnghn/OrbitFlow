package com.finalproj.orbitflow.hr.orgCategory.repository;

import com.finalproj.orbitflow.hr.orgCategory.entity.OrgCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgCategoryRepository
 * @since : 2025-12-16 화요일
 */
public interface OrgCategoryRepository extends JpaRepository<OrgCategory, Long> {

    /**
     * 회사별 조직 카테고리 전체 조회 (정렬 포함)
     */
    List<OrgCategory> findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(Long companyId);

    /**
     * 회사별 특정 조직 카테고리 조회
     */
    Optional<OrgCategory> findByCompanyIdAndId(Long companyId, Long categoryId);

    /**
     * 조직 카테고리 중복 체크
     */
    boolean existsByCompanyIdAndName(Long companyId, String name);

    @Query("""
                select max(o.orderIndex)
                from OrgCategory o
                where o.companyId = :companyId
            """)
    Integer findMaxOrderIndexByCompanyId(Long companyId);

    long countByCompanyIdAndIsActiveTrue(Long companyId);

    List<OrgCategory> findByCompanyIdAndIsActiveTrueAndNameContainingIgnoreCaseOrderByOrderIndexAsc(
            Long companyId, String keyword);

    boolean existsByCompanyIdAndNameAndIdNot(Long companyId, String name, Long id);
}
