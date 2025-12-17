package com.finalproj.orbitflow.approval.formTemplate.repository;

import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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

    @Query("""
    select distinct g.id as id, g.name as name
    from FormTemplateGroup g
    join FormTemplate t on t.templateGroup = g
    where g.company.id = :companyId
      and t.status = 'ACTIVE'
      and g.name like concat('%', :keyword, '%')
    order by g.name asc
    """)
    List<FormTemplateListView> findWithActiveTemplateAndCompanyAndKeyword(
            Long companyId,
            String keyword
    );
}
