package com.finalproj.orbitflow.approval.line.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalLineRuleDto
 * @since : 25. 12. 25. 목요일
 **/


@Getter
@AllArgsConstructor
public class ApprovalLineRuleDto {

    private List<ApprovalLineRuleStep> steps;
}