package com.finalproj.orbitflow.approval.form.template.group.dto;

import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
import com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateGroupListResDto
 * @since : 25. 12. 16. 화요일
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplateGroupListResDto {
    private Long id;
    private String name;
    private String description;
    private TemplateCategoryCode categoryCode;
    private BaseRole baseRole;
    private Boolean active;
    private Boolean hasActiveTemplate;
}
