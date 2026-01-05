package com.finalproj.orbitflow.approval.documentAISummary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiSummaryField
 * @since : 26. 1. 5. 월요일
 **/


@Getter
@AllArgsConstructor
public class AiSummaryField {
    private String label;
    private String fieldType;
    private Object value;
    private Map<String, Object> meta;
}