package com.finalproj.orbitflow.approval.document.ai.dto;

import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import org.springframework.lang.Nullable;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DiffPromptContext
 * @since : 26. 1. 6. 화요일
 **/


public record DiffPromptContext(
        AiDiffReqDto diff,
        FormTemplateSchema schema,
        @Nullable String rejectComment
) {
}
