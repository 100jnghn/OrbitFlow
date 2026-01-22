package com.finalproj.orbitflow.approval.document.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiSummaryReqDto
 * @since : 26. 1. 5. 월요일
 **/


@Getter
@AllArgsConstructor
public class AiSummaryReqDto {

    private String documentTitle;

    private List<AiSummaryField> coreFields;
    private List<AiSummaryField> optionalFields;
    private List<String> attachmentNames; // 파일명만
}
