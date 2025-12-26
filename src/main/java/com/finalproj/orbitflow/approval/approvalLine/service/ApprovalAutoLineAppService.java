package com.finalproj.orbitflow.approval.approvalLine.service;

import com.finalproj.orbitflow.hr.organization.dto.OrgResDto;
import com.finalproj.orbitflow.hr.organization.service.OrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalAutoLineAppService
 * @since : 25. 12. 25. 목요일
 **/

@Service
@RequiredArgsConstructor
public class ApprovalAutoLineAppService {
    private final ApprovalLineService approvalLineService;
    private final OrgService orgService;

    public void generate(Long orgId, Long formTemplateId, Long documentId) {
        List<OrgResDto> orgs = orgService.findOrgsByEmployeeId(orgId);

        approvalLineService.createApprovalLineByRule(orgs, formTemplateId, documentId);
    }

}
