package com.finalproj.orbitflow.approval.formTemplate.repository;

import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateRepository
 * @since : 25. 12. 17. 수요일
 **/


public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long> {
    Optional<Integer> findMaxVersionByTemplateGroup_Id(Long templateGroupId);

    Optional<FormTemplate> findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(Long id, FormTemplateStatus formTemplateStatus);
}
