package com.finalproj.orbitflow.approval.formTemplate.dto;

import com.finalproj.orbitflow.approval.formTemplate.enums.AffectTag;
import com.finalproj.orbitflow.approval.formTemplate.enums.FormTemplateStatus;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateAllListView;
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
                .build();
    }
}
