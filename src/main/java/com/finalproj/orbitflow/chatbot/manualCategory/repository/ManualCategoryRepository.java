package com.finalproj.orbitflow.chatbot.manualCategory.repository;

import com.finalproj.orbitflow.chatbot.manualCategory.entity.ManualCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ManualCategoryRepository
 * @since : 2025. 12. 30. 화요일
 */

@Repository
public interface ManualCategoryRepository extends JpaRepository<ManualCategory,Long> {
    Optional<ManualCategory> findById(Long categoryId);
    
    List<ManualCategory> findByCompanyIdAndIsActiveTrueOrderBySortOrderAsc(Long companyId);
}
