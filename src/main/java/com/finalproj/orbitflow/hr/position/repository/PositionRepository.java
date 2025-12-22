package com.finalproj.orbitflow.hr.position.repository;

import com.finalproj.orbitflow.hr.position.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionRepository
 * @since : 2025-12-16 화요일
 */
public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByCompanyIdAndIsActiveTrue(Long companyId);

    /* 조회 */
    List<Position> findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(Long companyId);

    List<Position> findByCompanyIdOrderByIsActiveDescOrderIndexAscCreatedAtDesc(Long companyId);

    /* 비활성화 검증 */
    boolean existsByCompanyIdAndCategoryIdAndIsActiveTrue(Long companyId, Long categoryId);

    boolean existsByCompanyIdAndParentPositionIdAndIsActiveTrue(Long companyId, Long parentPositionId);

    /* 정렬 */
    @Query("""
                select max(p.orderIndex)
                from Position p
                where p.company.id = :companyId
                  and p.isActive = true
            """)
    Integer findMaxCompanyActiveOrderIndex(
            @Param("companyId") Long companyId
    );

    long countByCompanyIdAndIsActiveTrue(Long companyId);
}