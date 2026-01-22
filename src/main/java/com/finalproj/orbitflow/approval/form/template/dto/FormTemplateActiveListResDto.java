package com.finalproj.orbitflow.approval.form.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateActiveListResDto
 * @since : 25. 12. 17. 수요일
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplateActiveListResDto {
    private Long formTemplateId;
    private int formTemplateVersion;
    private Long formTemplateGroupId;
    private String formTemplateGroupName;
}
