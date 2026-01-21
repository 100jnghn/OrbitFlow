package com.finalproj.orbitflow.approval.document.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateSchemaParser
 * @since : 26. 1. 21. 수요일
 **/


@Component
@RequiredArgsConstructor
public class FormTemplateSchemaParser {

    private final ObjectMapper objectMapper;

    public FormTemplateSchema parse(String json) {
        try {
            return objectMapper.readValue(json, FormTemplateSchema.class);
        } catch (Exception e) {
            throw new IllegalStateException("template_json 파싱 실패", e);
        }
    }
}