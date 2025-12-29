package com.finalproj.orbitflow.approval.formTemplateGroup.dto;

import com.finalproj.orbitflow.approval.formTemplateGroup.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
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


    public static FormTemplateGroupListResDto from(FormTemplateGroup formTemplateGroup) {
        return FormTemplateGroupListResDto.builder()
                .id(formTemplateGroup.getId())
                .name(formTemplateGroup.getName())
                .description(formTemplateGroup.getDescription())
                .categoryCode(formTemplateGroup.getTemplateCategory().getCode())
                .baseRole(formTemplateGroup.getBaseRole())
                .build();
    }
}
