package com.finalproj.orbitflow.approval.form.template.dto;

import com.finalproj.orbitflow.approval.form.template.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode;
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
