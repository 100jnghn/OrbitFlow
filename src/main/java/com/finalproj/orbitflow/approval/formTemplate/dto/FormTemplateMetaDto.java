package com.finalproj.orbitflow.approval.formTemplate.dto;

import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
import lombok.Builder;
import lombok.Data;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateMetaDto
 * @since : 25. 12. 21. 일요일
 **/

@Data
@Builder
public class FormTemplateMetaDto {
    private Long formTemplateId;
    private int version;
    private FormTemplateStatus status;
    private TemplateCategoryCode categoryCode;

    public static FormTemplateMetaDto from(FormTemplate formTemplate) {

        return FormTemplateMetaDto.builder()
                .formTemplateId(formTemplate.getId())
                .version(formTemplate.getVersion())
                .status(formTemplate.getStatus())
                .categoryCode(formTemplate.getTemplateCategory().getCode())
                .build();
    }
}
