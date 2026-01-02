package com.finalproj.orbitflow.approval.document.schema;

import lombok.Getter;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentField
 * @since : 25. 12. 31. 수요일
 **/


@Getter
public class DocumentField {

    private final String fieldId;
    private final String fieldType;
    private final String label;
    private final Map<String, Object> value;

    public DocumentField(
            String fieldId,
            String fieldType,
            String label,
            Map<String, Object> value
    ) {
        this.fieldId = fieldId;
        this.fieldType = fieldType;
        this.label = label;
        this.value = value;
    }
}
