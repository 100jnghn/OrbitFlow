package com.finalproj.orbitflow.approval.form.template.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateJson
 * @since : 26. 1. 8. 목요일
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormTemplateJson {
    List<FormField> fields;
}
