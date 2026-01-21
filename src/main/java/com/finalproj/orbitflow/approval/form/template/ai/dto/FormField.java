package com.finalproj.orbitflow.approval.form.template.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormField
 * @since : 26. 1. 8. 목요일
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormField {
    private String fieldId;
    private String fieldType;
    private String label;
    private int order;
    private boolean required;
    private Map<String, Object> meta;
}
