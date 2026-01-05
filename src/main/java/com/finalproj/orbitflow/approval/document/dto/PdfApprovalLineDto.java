package com.finalproj.orbitflow.approval.document.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfApprovalLineDto
 * @since : 26. 1. 5. 월요일
 **/


@Getter
@AllArgsConstructor
public class PdfApprovalLineDto {

    private final List<PdfApproverDto> approvers;

}