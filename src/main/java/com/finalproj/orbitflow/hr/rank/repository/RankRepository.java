package com.finalproj.orbitflow.hr.rank.repository;

import com.finalproj.orbitflow.hr.rank.entity.HrRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankRepository
 * @since : 2025-12-16 화요일
 */
public interface RankRepository extends JpaRepository<HrRank, Long> {


    List<HrRank> findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(Long companyId);

    List<HrRank> findByCompanyIdOrderByIsActiveDescOrderIndexAscCreatedAtDesc(Long companyId);

    List<HrRank> findByCompanyIdAndIsActiveTrueAndNameContainingIgnoreCaseOrderByOrderIndexAsc(
            Long companyId, String keyword);

    boolean existsByCompanyIdAndName(Long companyId, String name);

    boolean existsByCompanyIdAndNameAndIdNot(Long companyId, String name, Long id);

    long countByCompanyIdAndIsActiveTrue(Long companyId);

    @Query("""
                select max(r.orderIndex)
                from HrRank r
                where r.company.id = :companyId
                  and r.isActive = true
            """)
    Integer findMaxActiveOrderIndex(Long companyId);
}