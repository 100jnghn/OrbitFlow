package com.finalproj.orbitflow.approval.formTemplate.dto;

import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateUpdateReqDto
 * @since : 25. 12. 17. 수요일
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplateUpdateReqDto {
    private TemplateCategoryCode categoryCode;
    private String affectTags;
    private String templateJson;
    private String approvalRuleJson;
}