package com.finalproj.orbitflow.approval.form.template.group.dto;

import com.finalproj.orbitflow.approval.form.template.group.entity.FormTemplateGroup;
import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
import com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateGroupDetail
 * @since : 25. 12. 16. 화요일
 **/

@Builder
@Data
@AllArgsConstructor
public class FormTemplateGroupDetailResDto {
    private Long id;
    private String name;
    private String description;
    private TemplateCategoryCode categoryCode;
    private BaseRole baseRole;
    private Long createdBy;
    private Instant createdAt;
    private Instant updatedAt;


    public static FormTemplateGroupDetailResDto from(FormTemplateGroup entity) {
        return FormTemplateGroupDetailResDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .categoryCode(entity.getTemplateCategory().getCode())
                .baseRole(entity.getBaseRole())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
