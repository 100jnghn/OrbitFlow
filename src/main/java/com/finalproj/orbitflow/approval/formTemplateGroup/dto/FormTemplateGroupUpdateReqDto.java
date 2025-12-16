package com.finalproj.orbitflow.approval.formTemplateGroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateGroupUpdateReqDto
 * @since : 25. 12. 16. 화요일
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplateGroupUpdateReqDto {
    private String name;
    private String description;
    private Boolean active;
}
