package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.approval.document.enums.FieldType;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormFieldSchema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFormFieldDto
 * @since : 25. 12. 23. 화요일
 **/

@Data
@Builder
public class DocumentFormFieldDto {
    private String fieldId;
    private String fieldType;
    private String label;
    private boolean required;
    private Integer order;
    private Map<String, Object> meta;
    private Object value;
    private boolean editable;

    public static DocumentFormFieldDto from(FormFieldSchema formFieldSchema) {

        FieldType fieldType = FieldType.from(formFieldSchema.getFieldType());

        return DocumentFormFieldDto.builder()
                .fieldId(formFieldSchema.getFieldId())
                .fieldType(formFieldSchema.getFieldType())
                .label(formFieldSchema.getLabel())
                .required(formFieldSchema.isRequired())
                .order(formFieldSchema.getOrder())
                .meta(formFieldSchema.getMeta())
                .value(null)
                .editable(fieldType.isEditable())
                .build();
    }
}
