package com.finalproj.orbitflow.chatbot.manual.repository;

import com.finalproj.orbitflow.chatbot.manual.entity.ManualMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ManualRepository
 * @since : 2025. 12. 30. 화요일
 */

@Repository
public interface ManualRepository extends JpaRepository<ManualMetadata, Long> {

    List<ManualMetadata> findAllByCompanyIdAndIsActiveTrueOrderByIdDesc(Long companyId);

    List<ManualMetadata> findByCompanyIdAndCategoryIdAndIsActiveTrueOrderByIdDesc(Long companyId, Long categoryId);

    boolean existsByCompanyIdAndCategoryIdAndIsActiveTrue(Long companyId, Long categoryId);

    Optional<ManualMetadata> findByIdAndCompanyId(Long manualId, Long companyId);
}
