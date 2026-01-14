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

    /**
     * 결재선 DRAFT 초기화
     */
    public void initializeDraftLines(
            Document document,
            FormTemplate formTemplate,
            Employee writer
    ) {
        Company company = document.getCompany();

        // 기존 DRAFT 제거
        approvalLineRepository.deleteByDocumentAndStatus(
                document,
                ApprovalStatus.DRAFT
        );
        approvalLineRepository.flush();

        List<OrgResDto> userOrgs =
                orgService.findOrgsByOrgId(writer.getOrganization().getId());

        if (userOrgs == null || userOrgs.isEmpty()) {
            throw new BusinessException("사용자 조직 정보가 비어 있습니다.");
        }

        List<RawApprovalRuleStepDto> rawSteps =
                approvalRuleParser.parse(formTemplate.getApprovalRuleJson());

        if (rawSteps.isEmpty()) {
            throw new BusinessException("결재 규칙에 step이 없습니다.");
        }

        ApprovalLineRuleDto rule =
                approvalRuleMapper.convert(rawSteps);

        // 1️⃣ 결재선 전체 생성 (DRAFT)
        List<ApprovalLine> draftLines = new ArrayList<>();
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

                draftLines.addAll(lines);
                orderNo += lines.size();
                continue;
            }

            ApprovalLine line =
                    createSingleDraftApprovalLine(
                            document,
                            company,
                            ruleStep,
                            orderNo++
                    );

            draftLines.add(line);
        }

        // 2️⃣ 연속 중복 결재자 제거
        List<ApprovalLine> normalized =
                compressConsecutiveDuplicateApprovers(draftLines);

        // 3️⃣ orderNo 재정렬
        reorderOrderNo(normalized);

        // 4️⃣ 저장
        approvalLineRepository.saveAll(normalized);

        // 5️⃣ 최초 WAITING 활성화
        activateFirstWaitingLine(document);
    }

    /**
     * 결재 규칙 검증
     */
    public void validateApprovalRule(FormTemplate formTemplate) {
        List<RawApprovalRuleStepDto> rawSteps =
                approvalRuleParser.parse(formTemplate.getApprovalRuleJson());

        if (rawSteps.isEmpty()) {
            throw new BusinessException("결재 규칙에 step이 없습니다.");
        }

        approvalRuleMapper.convert(rawSteps);
    }

    /**
     * ORG_CATEGORY_CHAIN 처리
     * <p>
     * 정책:
     * - 조직 체인 결재선에서는 실제 결재자가 존재하는 단계만 결재선으로 생성한다.
     * - 결재자가 없는 조직 단계는 결재선에서 제거한다.
     * - 상신 시점에서는 모든 결재선에 결재자가 지정되어 있어야 하므로,
     * 생성 단계에서 null 결재선을 허용하지 않는다.
     */
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

        List<OrgResDto> chain = new ArrayList<>();
        OrgResDto current = leaf;

        while (current != null) {
            chain.add(current);

            if (Objects.equals(current.getCategoryId(), targetCategoryId)) {
                break;
            }

            current = current.getParentOrgId() == null
                    ? null
                    : orgMap.get(current.getParentOrgId());
        }

        List<ApprovalLine> result = new ArrayList<>();
        int orderNo = startOrderNo;

        for (OrgResDto org : chain) {

            PositionCategory positionCategory =
                    positionCategoryRepository
                            .findHeadPositionByOrgCategoryId(org.getCategoryId())
                            .orElse(null);

            if (positionCategory == null) {
                // 해당 조직 카테고리에 책임 직책이 없는 경우 결재선에서 제외
                continue;
            }

            Employee head =
                    employeeRepository
                            .findHeadByOrgIdAndPositionCategoryIdAndStatus(
                                    org.getId(),
                                    positionCategory.getId(),
                                    EmployeeStatus.ACTIVE
                            )
                            .orElse(null);

            if (head == null) {
                // 실제 결재자가 없는 조직 단계는 결재선에서 제거
                continue;
            }

            result.add(
                    ApprovalLine.builder()
                            .document(document)
                            .company(company)
                            .organization(
                                    orgRepository.getReferenceById(org.getId())
                            )
                            .positionCategory(positionCategory)
                            .approver(head)
                            .orderNo(orderNo++)
                            .status(ApprovalStatus.DRAFT)
                            .build()
            );
        }

        return result;
    }


    /**
     * 단일 결재선 생성
     */
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
                // 지정된 결재자가 유효한 경우 우선 사용하고,
                // 그렇지 않으면 조직/직책 기반 결재선으로 처리한다.
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
                        .ifPresent(builder::approver);
            }
        }

        return builder.build();
    }

    /**
     * 연속 중복 결재자 제거
     */
    private List<ApprovalLine> compressConsecutiveDuplicateApprovers(
            List<ApprovalLine> lines
    ) {
        List<ApprovalLine> result = new ArrayList<>();
        ApprovalLine prev = null;

        for (ApprovalLine curr : lines) {

            if (prev != null &&
                    prev.getApprover() != null &&
                    curr.getApprover() != null &&
                    Objects.equals(
                            prev.getApprover().getId(),
                            curr.getApprover().getId()
                    )) {
                continue;
            }

            result.add(curr);
            prev = curr;
        }

        return result;
    }

    /**
     * orderNo 재정렬
     */
    private void reorderOrderNo(List<ApprovalLine> lines) {
        int orderNo = 1;
        for (ApprovalLine line : lines) {
            line.changeOrderNo(orderNo++);
        }
    }

    /**
     * 최초 WAITING 활성화
     */
    private void activateFirstWaitingLine(Document document) {
        approvalLineRepository
                .findFirstByDocumentAndStatusOrderByOrderNoAsc(
                        document,
                        ApprovalStatus.DRAFT
                )
                .ifPresent(ApprovalLine::markWaiting);
    }
}