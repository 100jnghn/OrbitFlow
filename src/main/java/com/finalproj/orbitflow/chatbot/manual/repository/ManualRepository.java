package com.finalproj.orbitflow.chatbot.manual.repository;

import com.finalproj.orbitflow.chatbot.manual.entity.ManualMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ManualRepository
 * @since : 2025. 12. 30. 화요일
 */

@Repository
public interface ManualRepository extends JpaRepository<ManualMetadata, Long> {

    // 1. [관리자용] 삭제되지 않은(활성) 매뉴얼 전체 목록 조회
    List<ManualMetadata> findAllByCompanyIdAndIsActiveTrueOrderByIdDesc(Long companyId);

    // 2. [관리자용] 특정 카테고리의 활성 매뉴얼 목록 조회
    List<ManualMetadata> findByCompanyIdAndCategoryIdAndIsActiveTrueOrderByIdDesc(Long companyId, Long categoryId);

    boolean existsByCompanyIdAndCategoryIdAndIsActiveTrue(Long companyId, Long categoryId);
}
