package com.finalproj.orbitflow.approval.document.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiDiffReqDto
 * @since : 26. 1. 5. 월요일
 **/


@Getter
@AllArgsConstructor
public class AiDiffReqDto {

    private AiSummaryReqDto before;
    private AiSummaryReqDto current;
}