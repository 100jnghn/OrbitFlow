package com.finalproj.orbitflow.approval.formTemplateGroup.repository;

import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateGroupRepository
 * @since : 25. 12. 16. 화요일
 **/


public interface FormTemplateGroupRepository extends JpaRepository<FormTemplateGroup, Long> {


    @Query("""
    select f
    from FormTemplateGroup f
    where f.company.id = :companyId
      and f.name like concat('%', :keyword, '%')
    order by f.name asc
    """)
    List<FormTemplateGroupListView> findByCompanyAndKeyword(
            Long companyId,
            String keyword
    );


}
