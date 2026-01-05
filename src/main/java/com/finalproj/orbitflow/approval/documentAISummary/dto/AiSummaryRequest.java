package com.finalproj.orbitflow.approval.documentAISummary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiSummaryRequest
 * @since : 26. 1. 5. 월요일
 **/


@Getter
@AllArgsConstructor
public class AiSummaryRequest {

    private String documentTitle;

    private List<AiSummaryField> coreFields;
    private List<AiSummaryField> optionalFields;
}
