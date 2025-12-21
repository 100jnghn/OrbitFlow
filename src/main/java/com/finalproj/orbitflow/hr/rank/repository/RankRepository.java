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

    // 목록 - 비활성 제외
    List<HrRank> findByCompanyIdAndIsActiveTrueOrderByOrderIndexAsc(Long companyId);

    // 목록 - 비활성 포함
    List<HrRank> findByCompanyIdOrderByOrderIndexAsc(Long companyId);

    // 검색 - 비활성 제외
    List<HrRank> findByCompanyIdAndIsActiveTrueAndNameContainingIgnoreCaseOrderByOrderIndexAsc(Long companyId, String keyword);

    // 검색 - 비활성 포함
    List<HrRank> findByCompanyIdAndNameContainingIgnoreCaseOrderByOrderIndexAsc(Long companyId, String keyword);

    // 중복 체크
    boolean existsByCompanyIdAndName(Long companyId, String name);

    // 수정 시 중복 체크 (자기 자신 제외)
    boolean existsByCompanyIdAndNameAndIdNot(Long companyId, String name, Long id);

    // 상위 직급 선택용(활성만, 자기 자신 제외)
    List<HrRank> findByCompanyIdAndIsActiveTrueAndIdNotOrderByOrderIndexAsc(Long companyId, Long id);

    long countByCompanyIdAndIsActiveTrue(Long companyId);
}