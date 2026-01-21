package com.finalproj.orbitflow.approval.document.ai.dto;

import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SummaryPromptContext
 * @since : 26. 1. 6. 화요일
 **/


public record SummaryPromptContext(
        AiSummaryReqDto request,
        FormTemplateSchema schema
) {
}
