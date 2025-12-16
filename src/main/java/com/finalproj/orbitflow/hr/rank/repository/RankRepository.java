package com.finalproj.orbitflow.hr.rank.repository;

import com.finalproj.orbitflow.hr.rank.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankRepository
 * @since : 2025-12-16 화요일
 */
public interface RankRepository extends JpaRepository<Rank, Long> {

    List<Rank> findByCompanyIdAndIsActiveTrue(Long companyId);
}