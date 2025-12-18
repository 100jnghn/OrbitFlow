package com.finalproj.orbitflow.approval.formTemplate.dto;

import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag;
import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateDetailResDto
 * @since : 25. 12. 18. 목요일
 **/


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormTemplateDetailResDto {
    private Long formTemplateId;
    private Long templateGroupId;
    private String formTemplateName;
    private TemplateCategoryCode templateCategoryCode;
    private int version;
    private List<AffectTag> affectTags;
    private Object templateJson;
    private Object approvalRule;

    static public FormTemplateDetailResDto from(FormTemplate entity, ObjectMapper mapper) {
        try {
            return FormTemplateDetailResDto.builder()
                    .formTemplateId(entity.getId())
                    .templateGroupId(entity.getTemplateGroup().getId())
                    .formTemplateName(entity.getTemplateGroup().getName())
                    .templateCategoryCode(entity.getTemplateCategory().getCode())
                    .version(entity.getVersion())
                    .affectTags(entity.getAffectTags())
                    .templateJson(mapper.readValue(entity.getTemplateJson(), Object.class))
                    .approvalRule(mapper.readValue(entity.getApprovalRuleJson(), Object.class))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("FormTemplate JSON 파싱 실패", e);
        }
    }

}
