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
import com.finalproj.orbitflow.approval.formTemplate.entity.FormTemplate;
import com.finalproj.orbitflow.global.exception.BusinessException;
import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.organization.dto.OrgResDto;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.organization.service.OrgService;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import com.finalproj.orbitflow.hr.positionCategory.repository.PositionCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalLineDomainService
 * @since : 25. 12. 28. 일요일
 **/

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalLineDomainService {

    private final ApprovalLineRepository approvalLineRepository;
    private final ApprovalRuleParser approvalRuleParser;
    private final ApprovalRuleMapper approvalRuleMapper;
    private final OrgService orgService;
    private final EmployeeRepository employeeRepository;
    private final OrgRepository orgRepository;
    private final PositionCategoryRepository positionCategoryRepository;

    public void initializeDraftLines(
            Document document,
            FormTemplate formTemplate,
            Employee writer
    ) {
        Company company = document.getCompany();

        approvalLineRepository.deleteByDocumentAndStatus(
                document,
                ApprovalStatus.DRAFT
        );
        approvalLineRepository.flush();

        List<OrgResDto> userOrgs =
                orgService.findOrgsByEmployeeId(writer.getId());

        if (userOrgs == null || userOrgs.isEmpty()) {
            log.error(
                    "[ApprovalLine] user has no organizations. employeeId={}, documentId={}",
                    writer.getId(),
                    document.getId()
            );
            throw new BusinessException("사용자 조직 정보가 비어 있습니다.");
        }

        List<RawApprovalRuleStepDto> rawSteps =
                approvalRuleParser.parse(formTemplate.getApprovalRuleJson());

        if (rawSteps.isEmpty()) {
            throw new IllegalStateException(
                    "Invalid state: approval rule was validated but contains no steps. templateId="
                            + formTemplate.getId()
            );
        }

        ApprovalLineRuleDto rule =
                approvalRuleMapper.convert(rawSteps);

        int orderNo = 1;

        for (ApprovalLineRuleStep ruleStep : rule.getSteps()) {

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

    public void validateApprovalRule(FormTemplate formTemplate) {
        List<RawApprovalRuleStepDto> rawSteps =
                approvalRuleParser.parse(formTemplate.getApprovalRuleJson());

        if (rawSteps.isEmpty()) {
            throw new BusinessException("결재 규칙에 step이 없습니다.");
        }

        approvalRuleMapper.convert(rawSteps);
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

        Map<Long, OrgResDto> orgMap = new HashMap<>();
        Set<Long> referencedAsParent = new HashSet<>();

        for (OrgResDto o : userOrgs) {
            orgMap.put(o.getId(), o);
            if (o.getParentOrgId() != null) {
                referencedAsParent.add(o.getParentOrgId());
            }
        }

        OrgResDto leaf = userOrgs.stream()
                .filter(o -> !referencedAsParent.contains(o.getId()))
                .findFirst()
                .orElse(userOrgs.get(0));

        if (!referencedAsParent.contains(leaf.getId())) {
            log.warn(
                    "[ApprovalLine] ORG_CHAIN leaf fallback used. orgId={}",
                    leaf.getId()
            );
        }

        List<OrgResDto> chain = new ArrayList<>();
        OrgResDto current = leaf;

        while (current != null) {
            chain.add(current);

            if (Objects.equals(current.getCategoryId(), targetCategoryId)) {
                break;
            }

            Long parentId = current.getParentOrgId();
            if (parentId == null) break;

            current = orgMap.get(parentId);
            if (current == null) {
                log.warn(
                        "[ApprovalLine] ORG_CHAIN broken. missing parent. leafOrgId={}, missingParentId={}",
                        leaf.getId(),
                        parentId
                );
                break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(
                    "[ApprovalLine] ORG_CHAIN resolved. chainSize={}, targetCategoryId={}",
                    chain.size(),
                    targetCategoryId
            );
        }

        List<ApprovalLine> result = new ArrayList<>();
        int orderNo = startOrderNo;

        for (OrgResDto org : chain) {

            Organization organization =
                    orgRepository.getReferenceById(org.getId());

            PositionCategory positionCategory =
                    positionCategoryRepository
                            .findHeadPositionByOrgCategoryId(org.getCategoryId())
                            .orElse(null);

            if (positionCategory == null) {
                log.warn(
                        "[ApprovalLine] no head position. orgId={}, orgCategoryId={}",
                        org.getId(),
                        org.getCategoryId()
                );
            }

            Employee head = null;
            if (positionCategory != null) {
                head =
                        employeeRepository
                                .findHeadByOrgIdAndPositionCategoryIdAndStatus(
                                        org.getId(),
                                        positionCategory.getId(),
                                        EmployeeStatus.ACTIVE
                                )
                                .orElse(null);
            }

            if (head == null) {
                log.warn(
                        "[ApprovalLine] no head employee. orgId={}, positionCategoryId={}",
                        org.getId(),
                        positionCategory == null ? null : positionCategory.getId()
                );
            }

            result.add(
                    ApprovalLine.builder()
                            .document(document)
                            .company(company)
                            .organization(organization)
                            .positionCategory(positionCategory)
                            .approver(head)
                            .orderNo(orderNo++)
                            .status(ApprovalStatus.DRAFT)
                            .build()
            );
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

        ApprovalLine.ApprovalLineBuilder builder =
                ApprovalLine.builder()
                        .document(document)
                        .company(company)
                        .orderNo(orderNo)
                        .status(ApprovalStatus.DRAFT);

        switch (target.getType()) {

            case ORG_AND_POSITION -> builder
                    .organization(
                            orgRepository.getReferenceById(
                                    target.getOrganizationId()
                            )
                    )
                    .positionCategory(
                            positionCategoryRepository.getReferenceById(
                                    target.getPositionId()
                            )
                    );

            case FIXED_EMPLOYEE -> {

                if (target.getEmployeeId() == null) {
                    log.warn(
                            "[ApprovalLine] FIXED_EMPLOYEE missing employeeId. ruleStep={}",
                            ruleStep
                    );
                    break;
                }

                builder
                        .organization(
                                orgRepository.getReferenceById(
                                        target.getOrganizationId()
                                )
                        )
                        .positionCategory(
                                positionCategoryRepository.getReferenceById(
                                        target.getPositionId()
                                )
                        );

                employeeRepository
                        .findByIdAndOrgIdAndPositionCategoryIdAndStatus(
                                target.getEmployeeId(),
                                target.getOrganizationId(),
                                target.getPositionId(),
                                EmployeeStatus.ACTIVE
                        )
                        .ifPresentOrElse(
                                builder::approver,
                                () -> log.warn(
                                        "[ApprovalLine] FIXED_EMPLOYEE not found. employeeId={}, orgId={}, positionId={}",
                                        target.getEmployeeId(),
                                        target.getOrganizationId(),
                                        target.getPositionId()
                                )
                        );
            }
        }

        return builder.build();
    }
}
