package com.finalproj.orbitflow.approval.formTemplate.dto;

import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.templateCategory.enums.TemplateCategoryCode;
import lombok.Data;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateAllListReqDto
 * @since : 25. 12. 31. 수요일
 **/

@Data
public class FormTemplateAllListReqDto {
    private String keyword;
    private FormTemplateStatus status;
    private TemplateCategoryCode templateCategoryCode;
}
