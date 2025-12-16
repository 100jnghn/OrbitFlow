package com.finalproj.orbitflow.approval.formTemplateGroup.dto;

import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import com.finalproj.orbitflow.hr.company.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateGroupReqDto
 * @since : 25. 12. 16. 화요일
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplateGroupCreateReqDto {
    private String name;
    private String description;

    public FormTemplateGroup toEntity(Company company) {
        return FormTemplateGroup.builder()
                .name(this.name)
                .description(this.description)
                .company(company)
                .active(true)
                .build();
    }
}
