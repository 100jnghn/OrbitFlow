package com.finalproj.orbitflow.hr.positionCategory.repository;

import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionCategoryRepository
 * @since : 2025-12-16 화요일
 */
public interface PositionCategoryRepository extends JpaRepository<PositionCategory, Long> {

    List<PositionCategory> findByCompanyIdAndIsActiveTrue(Long companyId);
}