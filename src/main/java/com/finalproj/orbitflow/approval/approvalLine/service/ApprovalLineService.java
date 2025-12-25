package com.finalproj.orbitflow.approval.approvalLine.service;

import com.finalproj.orbitflow.approval.approvalLine.dto.ApprovalLineRuleDto;
import com.finalproj.orbitflow.approval.approvalLine.dto.ApprovalLineRuleStep;
import com.finalproj.orbitflow.approval.approvalLine.dto.RawApprovalRuleStepDto;
import com.finalproj.orbitflow.approval.approvalLine.dto.RuleTarget;
import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.approval.approvalLine.enums.ApprovalStatus;
import com.finalproj.orbitflow.approval.approvalLine.enums.RuleTargetType;
import com.finalproj.orbitflow.approval.approvalLine.repository.ApprovalLineRepository;
import com.finalproj.orbitflow.approval.document.entity.Document;
import com.finalproj.orbitflow.approval.document.repository.DocumentRepository;
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.approval.formTemplate.repository.FormTemplateRepository;
import com.finalproj.orbitflow.global.exception.BusinessException;
import com.finalproj.orbitflow.global.exception.NotFoundException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.organization.dto.OrgResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalLineService
 * @since : 25. 12. 25. 목요일
 **/

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalLineService {

    private final ApprovalLineRepository approvalLineRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;
    private final ApprovalRuleParser approvalRuleParser;   // ✅ 인터페이스
    private final ApprovalRuleMapper approvalRuleMapper;

    @Transactional
    public void createApprovalLineByRule(
            List<OrgResDto> userOrgs,
            Long formTemplateId,
            Long documentId
    ) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        FormTemplate formTemplate = formTemplateRepository.findById(formTemplateId)
                .orElseThrow(() -> new NotFoundException("Form template not found"));

        Company company = document.getCompany();

        // 기존 Draft 삭제
        approvalLineRepository.deleteByDocumentAndStatus(
                document,
                ApprovalStatus.DRAFT
        );

        // ✅ JSON → Raw Step List
        List<RawApprovalRuleStepDto> rawSteps =
                approvalRuleParser.parse(
                        formTemplate.getApprovalRuleJson()
                );

        // ✅ Raw → Rule
        ApprovalLineRuleDto rule =
                approvalRuleMapper.convert(rawSteps);

        int orderNo = 1;

        for (ApprovalLineRuleStep ruleStep : rule.getSteps()) {

            // 1️⃣ 조직 카테고리 체인
            if (ruleStep.getTarget().getType() == RuleTargetType.ORG_CATEGORY_CHAIN) {

                List<ApprovalLine> lines =
                        createOrgCategoryChainLines(
                                document,
                                company,
                                ruleStep,
                                userOrgs,
                                orderNo
                        );

                approvalLineRepository.saveAll(lines);
                orderNo += lines.size();
                continue;
            }

            // 2️⃣ 단일 라인
            ApprovalLine line =
                    createSingleDraftApprovalLine(
                            document,
                            company,
                            ruleStep,
                            orderNo
                    );

            approvalLineRepository.save(line);
            orderNo++;
        }
    }

    private List<ApprovalLine> createOrgCategoryChainLines(
            Document document,
            Company company,
            ApprovalLineRuleStep ruleStep,
            List<OrgResDto> userOrgs,
            int startOrderNo
    ) {
        Long targetCategoryId =
                ruleStep.getTarget().getOrganizationCategoryId();

        List<ApprovalLine> result = new ArrayList<>();
        int orderNo = startOrderNo;

        for (OrgResDto org : userOrgs) {

            Employee head =
                    employeeRepository.findHeadByOrganizationAndOrgCategory(
                            org.getId(),
                            org.getCategoryId()
                    ).orElseThrow(() ->
                            new BusinessException(
                                    "조직 책임자를 찾을 수 없습니다. orgId=" + org.getId()
                            )
                    );

            result.add(
                    ApprovalLine.builder()
                            .document(document)
                            .company(company)
                            .orderNo(orderNo++)
                            .approver(head)
                            .status(ApprovalStatus.DRAFT)
                            .build()
            );

            if (org.getCategoryId().equals(targetCategoryId)) {
                break;
            }
        }

        return result;
    }

    private ApprovalLine createSingleDraftApprovalLine(
            Document document,
            Company company,
            ApprovalLineRuleStep ruleStep,
            int orderNo
    ) {
        RuleTarget target = ruleStep.getTarget();

        ApprovalLine line = ApprovalLine.builder()
                .document(document)
                .company(company)
                .orderNo(orderNo)
                .status(ApprovalStatus.DRAFT)
                .approver(null)
                .build();

        switch (target.getType()) {

            case ORG_AND_POSITION -> {
                // 사용자 선택 (approver = null 유지)
            }

            case FIXED_EMPLOYEE -> {
                boolean exists =
                        employeeRepository.existsInOrgAndPositionCategory(
                                target.getEmployeeId(),
                                target.getOrganizationId(),
                                target.getPositionId()
                        );

                if (exists) {
                    Employee employee =
                            employeeRepository.getReferenceById(
                                    target.getEmployeeId()
                            );
                    line.setApprover(employee);
                }
            }
        }

        return line;
    }
}