package com.finalproj.orbitflow.approval.documentContent.dto;

import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentContentPatchReqDto
 * @since : 25. 12. 26. 금요일
 **/


@Data
public class DocumentContentPatchReqDto {

    private List<FieldValueDto> fields;

    @Data
    public static class FieldValueDto {
        private String fieldId;
        private JsonNode value;
    }
}