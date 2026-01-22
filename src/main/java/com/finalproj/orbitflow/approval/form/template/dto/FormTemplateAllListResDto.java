package com.finalproj.orbitflow.approval.form.template.dto;

import com.finalproj.orbitflow.approval.form.template.enums.AffectTag;
import com.finalproj.orbitflow.approval.form.template.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.form.template.repository.FormTemplateAllListView;
import com.finalproj.orbitflow.approval.form.template.category.enums.TemplateCategoryCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateAllListResDto
 * @since : 25. 12. 18. 목요일
 **/


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormTemplateAllListResDto {

    private Long formTemplateId;
    private String formTemplateGroupName;
    private int formTemplateVersion;
    private int useDocument;
    private Instant updatedAt;
    private List<AffectTag> affectTags;
    private FormTemplateStatus formTemplateStatus;
    private TemplateCategoryCode templateCategoryCode;

    public static FormTemplateAllListResDto from(
            FormTemplateAllListView view
    ) {
        return FormTemplateAllListResDto.builder()
                .formTemplateId(view.getFormTemplateId())
                .formTemplateGroupName(view.getFormTemplateGroupName())
                .formTemplateVersion(view.getFormTemplateVersion())
                .useDocument(view.getUseDocument())
                .updatedAt(view.getUpdatedAt())
                .affectTags(view.getAffectTags())
                .formTemplateStatus(view.getFormTemplateStatus())
                .templateCategoryCode(view.getTemplateCategoryCode())
                .build();
    }
}
