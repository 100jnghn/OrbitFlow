package com.finalproj.orbitflow.approval.logFormTemplateAI.dto;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormTemplateRequestDto
 * @since : 26. 1. 7. 수요일
 **/


public record FormTemplateRequestDto(
        Long templateGroupId,
        String formName,
        String purpose // nullable
) {
}