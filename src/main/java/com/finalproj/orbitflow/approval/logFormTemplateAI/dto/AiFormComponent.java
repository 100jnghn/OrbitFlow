package com.finalproj.orbitflow.approval.logFormTemplateAI.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiFormComponent
 * @since : 26. 1. 8. 목요일
 **/


@JsonIgnoreProperties(ignoreUnknown = true)
public record AiFormComponent(
        String type,
        String label,
        Boolean required,
        Map<String, Object> meta
) {
}