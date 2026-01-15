package com.finalproj.orbitflow.approval.logformtemplateai.repository;

import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.logformtemplateai.entity.LogFormTemplateAi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : LogFormTemplateAiRepository
 * @since : 26. 1. 7. 수요일
 **/


public interface LogFormTemplateAiRepository extends JpaRepository<LogFormTemplateAi, Long> {
    @Modifying
    @Query("""
            update LogFormTemplateAi l
            set l.createdTemplate = null
            where l.createdTemplate = :template
            """)
    void clearTemplateReference(@Param("template") FormTemplate template);
}
