package com.finalproj.orbitflow.approval.documentAISummary.dto;

import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;
import edu.umd.cs.findbugs.annotations.Nullable;

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
