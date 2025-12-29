package com.finalproj.orbitflow.approval.approvalLine.service;

import com.finalproj.orbitflow.approval.approvalLine.dto.RawApprovalRuleStepDto;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalRuleParser
 * @since : 25. 12. 25. 목요일
 **/


public interface ApprovalRuleParser {
    List<RawApprovalRuleStepDto> parse(String approvalRuleJson);
}
