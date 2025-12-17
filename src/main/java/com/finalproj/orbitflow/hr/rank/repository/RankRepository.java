package com.finalproj.orbitflow.hr.rank.repository;

import com.finalproj.orbitflow.hr.rank.entity.HrRank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankRepository
 * @since : 2025-12-16 화요일
 */
public interface RankRepository extends JpaRepository<HrRank, Long> {

    List<HrRank> findByCompanyIdAndIsActiveTrue(Long companyId);
}