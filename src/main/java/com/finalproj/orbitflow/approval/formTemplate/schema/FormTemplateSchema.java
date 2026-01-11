package com.finalproj.orbitflow.approval.formTemplate.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateSchema
 * @since : 25. 12. 21. 일요일
 **/


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormTemplateSchema {

    private List<FormFieldSchema> fields;
}