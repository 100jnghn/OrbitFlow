package com.finalproj.orbitflow.hr.position.repository;

import com.finalproj.orbitflow.hr.position.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

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
}