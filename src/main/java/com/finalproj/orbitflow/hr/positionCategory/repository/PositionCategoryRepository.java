package com.finalproj.orbitflow.hr.positionCategory.repository;

import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionCategoryRepository
 * @since : 2025-12-16 화요일
 */
public interface PositionCategoryRepository extends JpaRepository<PositionCategory, Long> {

    List<PositionCategory> findByCompanyId(Long companyId);

    List<PositionCategory> findByCompanyIdAndIsActiveTrue(Long companyId);

    long countByCompanyIdAndIsActiveTrue(Long companyId);

    boolean existsByCompanyIdAndName(Long companyId, String name);

    @Query("""
                select max(pc.orderIndex)
                from PositionCategory pc
                where pc.company.id = :companyId
                  and pc.isActive = true
            """)
    Integer findMaxActiveOrderIndexByCompanyId(@Param("companyId") Long companyId);

    List<PositionCategory> findByCompanyIdOrderByIsActiveDescOrderIndexAscCreatedAtDesc(Long companyId);

}