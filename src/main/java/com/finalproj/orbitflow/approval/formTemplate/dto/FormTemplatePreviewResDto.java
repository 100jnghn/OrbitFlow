package com.finalproj.orbitflow.approval.formTemplate.dto;

import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplatePreviewResDto
 * @since : 25. 12. 21. 일요일
 **/

@Getter
@AllArgsConstructor
@Builder
public class FormTemplatePreviewResDto {

    private FormTemplateMetaDto meta;

    private FormTemplateSchema contentSchema;

    public static FormTemplatePreviewResDto from(
            FormTemplateMetaDto meta,
            FormTemplateSchema contentSchema
    ) {
        return FormTemplatePreviewResDto.builder()
                .meta(meta)
                .contentSchema(contentSchema)
                .build();
    }
}
