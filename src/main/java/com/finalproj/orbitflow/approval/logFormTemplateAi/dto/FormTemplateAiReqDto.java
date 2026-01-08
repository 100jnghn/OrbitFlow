package com.finalproj.orbitflow.approval.logFormTemplateAi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateAiReqDto
 * @since : 26. 1. 8. 목요일
 **/


public record FormTemplateAiReqDto(
        @NotNull
        Long formTemplateId,

        @NotBlank
        String formName,

        String purpose // nullable
) {
}
