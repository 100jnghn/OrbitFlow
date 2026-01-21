package com.finalproj.orbitflow.approval.form.template.category.repository;

import com.finalproj.orbitflow.approval.form.template.category.entity.TemplateCategory;
import com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : TemplateCategoryRepository
 * @since : 25. 12. 17. 수요일
 **/


public interface TemplateCategoryRepository extends JpaRepository<TemplateCategory, Long> {
    Optional<TemplateCategory> findByCode(TemplateCategoryCode categoryCode);
}
