package com.finalproj.orbitflow.approval.formTemplate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateCreateReqDto
 * @since : 25. 12. 17. 수요일
 **/

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FormTemplateCreateReqDto {
    private Long templateGroupId;
    private String affectTags;
    private String templateJson;
    private String approvalRuleJson;


}
