package com.finalproj.orbitflow.approval.form.template.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiFormDesignResult
 * @since : 26. 1. 8. 목요일
 **/

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiFormDesignResult(
        List<AiFormComponent> components
) {
}
