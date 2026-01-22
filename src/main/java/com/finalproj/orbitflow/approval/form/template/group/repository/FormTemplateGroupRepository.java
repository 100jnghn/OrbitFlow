package com.finalproj.orbitflow.approval.form.template.group.repository;

import com.finalproj.orbitflow.approval.form.template.group.entity.FormTemplateGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateGroupRepository
 * @since : 25. 12. 16. 화요일
 **/


public interface FormTemplateGroupRepository extends JpaRepository<FormTemplateGroup, Long>, FormTemplateGroupRepositoryCustom {


    @Query("""
    select f
    from FormTemplateGroup f
    where f.company.id = :companyId
      and f.name like concat('%', :keyword, '%')
    order by f.name asc
    """)
    List<FormTemplateGroup> findByCompanyAndKeyword(
            Long companyId,
            String keyword
    );


    boolean existsByCompanyIdAndName(Long companyId, String name);

    Optional<FormTemplateGroup> findByCompany_IdAndName(Long companyId, String name);
}
