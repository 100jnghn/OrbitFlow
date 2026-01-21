package com.finalproj.orbitflow.approval.document.support.title;

import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentTitleResolver
 * @since : 26. 1. 21. 수요일
 **/


@Component
public class DocumentTitleResolver {

    public String resolve(FormTemplateSchema schema) {
        return schema.getFields().stream()
                .filter(f -> "document-title".equals(f.getFieldId()))
                .findFirst()
                .map(f -> {
                    Object value = f.getMeta().get("value");
                    if (value == null || value.toString().isBlank()) {
                        return "제목 없음";
                    }
                    return value.toString();
                })
                .orElse("제목 없음");
    }
}
