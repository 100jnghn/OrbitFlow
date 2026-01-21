package com.finalproj.orbitflow.approval.form.template.group.repository;

import com.finalproj.orbitflow.approval.form.template.group.dto.FormTemplateGroupListResDto;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateGroupRepositoryCustom
 * @since : 25. 12. 30. 화요일
 **/


public interface FormTemplateGroupRepositoryCustom {
    public List<FormTemplateGroupListResDto> findLatestGroupsWithActiveTemplate(
            Long companyId,
            String keyword
    );
}
