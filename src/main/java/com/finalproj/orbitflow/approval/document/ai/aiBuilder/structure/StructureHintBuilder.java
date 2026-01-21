package com.finalproj.orbitflow.approval.document.ai.aiBuilder.structure;

import com.finalproj.orbitflow.approval.formTemplate.schema.FormTemplateSchema;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : StructureHintBuilder
 * @since : 26. 1. 6. 화요일
 **/


public interface StructureHintBuilder {
    String build(FormTemplateSchema templateSchema);
}
