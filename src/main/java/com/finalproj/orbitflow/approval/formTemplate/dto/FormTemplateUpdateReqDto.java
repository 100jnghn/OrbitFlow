package com.finalproj.orbitflow.approval.formTemplate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

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
    private JsonNode templateJson;
    private JsonNode approvalRuleJson;
}