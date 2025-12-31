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
    List<ManualMetadata> findAllByCompanyIdOrderByIdDesc(Long companyId);
    
    List<ManualMetadata> findByCompanyIdAndCategoryIdOrderByIdDesc(Long companyId, Long categoryId);
}
