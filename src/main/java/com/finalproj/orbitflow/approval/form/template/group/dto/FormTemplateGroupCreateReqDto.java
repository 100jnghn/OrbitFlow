package com.finalproj.orbitflow.approval.form.template.group.dto;

import com.finalproj.orbitflow.approval.form.template.group.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
import com.finalproj.orbitflow.approval.form.template.category.entity.TemplateCategory;
import com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode;
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

    private TemplateCategoryCode categoryCode; // 프론트는 코드만 보냄
    private BaseRole baseRole;                 // ✅ 추가 (nullable)

    public FormTemplateGroup toEntity(Company company, TemplateCategory category) {
        return FormTemplateGroup.builder()
                .company(company)
                .name(this.name)
                .description(this.description)
                .templateCategory(category)
                .baseRole(this.baseRole)
                .active(true)
                .build();
    }
}