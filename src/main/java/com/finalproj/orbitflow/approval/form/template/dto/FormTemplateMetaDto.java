package com.finalproj.orbitflow.approval.form.template.dto;

import com.finalproj.orbitflow.approval.form.template.entity.FormTemplate;
import com.finalproj.orbitflow.approval.form.template.enums.FormTemplateStatus;
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

    public static FormTemplateMetaDto from(FormTemplate formTemplate) {

        return FormTemplateMetaDto.builder()
                .formTemplateId(formTemplate.getId())
                .version(formTemplate.getVersion())
                .status(formTemplate.getStatus())
                .build();
    }
}
