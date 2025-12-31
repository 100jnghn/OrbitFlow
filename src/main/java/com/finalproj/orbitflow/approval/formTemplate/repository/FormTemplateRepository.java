package com.finalproj.orbitflow.approval.formTemplate.repository;

import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    Optional<FormTemplate> findTopByTemplateGroup_IdAndStatusOrderByUpdatedAtDesc(Long templateGroupId, FormTemplateStatus status);

    Optional<FormTemplate> findTopByTemplateGroup_IdAndStatusOrderByVersionDesc(Long id, FormTemplateStatus formTemplateStatus);

    @Query("""
            select distinct t.id as id, g.name as name, t.version as version, g.id as groupId
            from FormTemplateGroup g
            join FormTemplate t on t.templateGroup = g
            where g.company.id = :companyId
              and t.status = 'ACTIVE'
              and g.name like concat('%', :keyword, '%')
            order by t.id
            """)
    List<FormTemplateListView> findWithActiveTemplateAndCompanyAndKeyword(
            Long companyId,
            String keyword
    );


    @Query("""
    select
        t.id as formTemplateId,
        g.name as formTemplateGroupName,
        t.version as formTemplateVersion,
        count(d.id) as useDocument,
        t.updatedAt as updatedAt,
        t.status as formTemplateStatus,
        t.affectTags as affectTags,
        c.code as templateCategoryCode
    from FormTemplate t
    join t.templateGroup g
    join g.templateCategory c
    left join Document d
      on d.templateGroup.id = g.id
     and d.templateVersion = t.version
    where g.company.id = :companyId
      and (:keyword is null or g.name like concat('%', :keyword, '%'))
      and (:status is null or t.status = :status)
      and (:categoryCode is null or c.code = :categoryCode)
    group by
        t.id, g.name, t.version, t.updatedAt,
        t.status, t.affectTags, c.code
    """)
    Page<FormTemplateAllListView> findAllWithDocumentCount(
            Long companyId,
            String keyword,
            FormTemplateStatus status,
            TemplateCategoryCode categoryCode,
            Pageable pageable
    );


    @Query("""
    select max(ft.version)
    from FormTemplate ft
    where ft.templateGroup.id = :templateGroupId
      and ft.status <> 'DRAFT'
    """)
    Optional<Integer> findMaxVersionByTemplateGroupId(
            @Param("templateGroupId") Long templateGroupId
    );


    Optional<FormTemplate> findByIdAndCompany_id(Long id, Long id1);
}



