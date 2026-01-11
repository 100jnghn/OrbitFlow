package com.finalproj.orbitflow.approval.document.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfApproverDto
 * @since : 26. 1. 5. 월요일
 **/


@Getter
@AllArgsConstructor
public class PdfApproverDto {

    private Long approverLineId;
    private int order;              // 결재 순서
    private String name;             // 이름
    private String position;         // 직책 (optional)
    private String signatureImageUrl; // 전자서명 이미지 (없으면 null)
}
