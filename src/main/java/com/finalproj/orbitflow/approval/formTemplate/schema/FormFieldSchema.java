package com.finalproj.orbitflow.approval.formTemplate.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FieldSchema
 * @since : 25. 12. 21. 일요일
 **/


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormFieldSchema {

    private String fieldId;
    private String fieldType;
    private String label;
    private boolean required;
    private Integer order;

    private Map<String, Object> meta;
}