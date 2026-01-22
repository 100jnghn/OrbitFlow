package com.finalproj.orbitflow.approval.line.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalLineRuleStep
 * @since : 25. 12. 25. 목요일
 **/


@Getter
@AllArgsConstructor
public class ApprovalLineRuleStep {

    private int step;
    private RuleTarget target;
}