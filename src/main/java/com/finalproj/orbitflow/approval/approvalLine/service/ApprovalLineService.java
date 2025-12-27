package com.finalproj.orbitflow.approval.approvalLine.service;

import com.finalproj.orbitflow.approval.approvalLine.dto.*;
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
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.hr.organization.dto.OrgResDto;
import com.finalproj.orbitflow.hr.organization.entity.Organization;
import com.finalproj.orbitflow.hr.organization.repository.OrgRepository;
import com.finalproj.orbitflow.hr.positionCategory.entity.PositionCategory;
import com.finalproj.orbitflow.hr.positionCategory.repository.PositionCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
@Slf4j
public class ApprovalLineService {

    private final ApprovalLineRepository approvalLineRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;
    private final ApprovalRuleParser approvalRuleParser;   // ✅ 인터페이스
    private final ApprovalRuleMapper approvalRuleMapper;
    private final OrgRepository orgRepository;
    private final PositionCategoryRepository positionCategoryRepository;

    @Transactional
    public void createApprovalLineByRule(
            List<OrgResDto> userOrgs,
            Long formTemplateId,
            Long documentId
    ) {
        log.info(
                "[ApprovalLine] createApprovalLineByRule START - documentId={}, formTemplateId={}, userOrgs={}",
                documentId,
                formTemplateId,
                userOrgs == null ? "null" : userOrgs.size()
        );

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    log.error("[ApprovalLine] Document not found - documentId={}", documentId);
                    return new NotFoundException("Document not found");
                });

        FormTemplate formTemplate = formTemplateRepository.findById(formTemplateId)
                .orElseThrow(() -> {
                    log.error("[ApprovalLine] FormTemplate not found - formTemplateId={}", formTemplateId);
                    return new NotFoundException("Form template not found");
                });

        log.info(
                "[ApprovalLine] approvalRuleJson(raw)={}",
                formTemplate.getApprovalRuleJson()
        );

        Company company = document.getCompany();

        log.debug(
                "[ApprovalLine] Loaded document & template - companyId={}, approvalRuleJsonLength={}",
                company.getId(),
                formTemplate.getApprovalRuleJson() == null ? 0 : formTemplate.getApprovalRuleJson().length()
        );

        // 기존 Draft 삭제
        int deletedCount =
                approvalLineRepository.deleteByDocumentAndStatus(
                        document,
                        ApprovalStatus.DRAFT
                );

        approvalLineRepository.flush(); // ⭐ 이거 필수

        log.info(
                "[ApprovalLine] Deleted existing DRAFT lines - count={}",
                deletedCount
        );

        // 1️⃣ JSON → Raw Steps
        List<RawApprovalRuleStepDto> rawSteps =
                approvalRuleParser.parse(formTemplate.getApprovalRuleJson());

        log.info(
                "[ApprovalLine] rawSteps={}",
                rawSteps
        );

        if (rawSteps.isEmpty()) {
            log.warn(
                    "[ApprovalLine] Approval rule has no steps - documentId={}, formTemplateId={}",
                    documentId,
                    formTemplateId
            );
            return;
        }

        // 2️⃣ Raw → Rule
        ApprovalLineRuleDto rule =
                approvalRuleMapper.convert(rawSteps);

        for (ApprovalLineRuleStep step : rule.getSteps()) {
            log.info(
                    "[ApprovalLine] ruleStep - stepNo={}, targetType={}, target={}",
                    step.getStep(),
                    step.getTarget().getType(),
                    step.getTarget()
            );
        }
        int orderNo = 1;

        for (ApprovalLineRuleStep ruleStep : rule.getSteps()) {

            log.debug(
                    "[ApprovalLine] Processing ruleStep - orderNo={}, targetType={}, step={}",
                    orderNo,
                    ruleStep.getTarget().getType(),
                    ruleStep
            );

            // 조직 카테고리 체인
            if (ruleStep.getTarget().getType() == RuleTargetType.ORG_CATEGORY_CHAIN) {

                List<ApprovalLine> lines =
                        createOrgCategoryChainLines(
                                document,
                                company,
                                ruleStep,
                                userOrgs,
                                orderNo
                        );

                log.info(
                        "[ApprovalLine] ORG_CATEGORY_CHAIN generated - lines={}, startOrderNo={}",
                        lines.size(),
                        orderNo
                );

                if (log.isDebugEnabled()) {
                    lines.forEach(l ->
                            log.debug(
                                    "  ↳ line: orderNo={}, employeeId={}, orgId={}",
                                    l.getOrderNo(),
                                    l.getApprover() == null ? null : l.getApprover().getId()
                            )
                    );
                }

                approvalLineRepository.saveAll(lines);
                orderNo += lines.size();
                continue;
            }

            // 단일 라인
            ApprovalLine line =
                    createSingleDraftApprovalLine(
                            document,
                            company,
                            ruleStep,
                            orderNo
                    );

            log.info(
                    "[ApprovalLine] SINGLE line generated - orderNo={}, approverId={}",
                    orderNo,
                    line.getApprover() == null ? null : line.getApprover().getId()
            );

            approvalLineRepository.save(line);
            orderNo++;
        }

        log.info(
                "[ApprovalLine] createApprovalLineByRule END - documentId={}, totalOrderNo={}",
                documentId,
                orderNo - 1
        );
    }


    private List<ApprovalLine> createOrgCategoryChainLines(
            Document document,
            Company company,
            ApprovalLineRuleStep ruleStep,
            List<OrgResDto> userOrgs,
            int startOrderNo
    ) {
        Long targetCategoryId = ruleStep.getTarget().getOrganizationCategoryId();

        if (userOrgs == null || userOrgs.isEmpty()) {
            throw new BusinessException("사용자 조직 정보가 비어 있습니다.");
        }

    /* =====================================================
       1) orgId -> OrgResDto 맵 구성
       ===================================================== */
        Map<Long, OrgResDto> orgMap = new HashMap<>();
        for (OrgResDto o : userOrgs) {
            orgMap.put(o.getId(), o);
        }

    /* =====================================================
       2) leaf 조직 찾기
          - parent로 참조되지 않는 노드
       ===================================================== */
        Set<Long> referencedAsParent = new HashSet<>();
        for (OrgResDto o : userOrgs) {
            if (o.getParentOrgId() != null) {
                referencedAsParent.add(o.getParentOrgId());
            }
        }

        OrgResDto leaf = null;
        for (OrgResDto o : userOrgs) {
            if (!referencedAsParent.contains(o.getId())) {
                leaf = o;
                break;
            }
        }

        // fallback
        if (leaf == null) {
            leaf = userOrgs.get(0);
            log.warn(
                    "[ApprovalLine] ORG_CHAIN leaf fallback used - orgId={}",
                    leaf.getId()
            );
        }

    /* =====================================================
       3) leaf -> parent 체인 구성 (하위 → 상위)
       ===================================================== */
        List<OrgResDto> chain = new ArrayList<>();
        OrgResDto current = leaf;

        while (current != null) {
            chain.add(current);

            if (current.getCategoryId() != null
                    && current.getCategoryId().equals(targetCategoryId)) {
                break;
            }

            Long parentId = current.getParentOrgId();
            if (parentId == null) break;

            current = orgMap.get(parentId);
            if (current == null) {
                log.warn(
                        "[ApprovalLine] ORG_CHAIN broken - missing parent in userOrgs. leafOrgId={}, missingParentId={}",
                        leaf.getId(),
                        parentId
                );
                break;
            }
        }

        log.info(
                "[ApprovalLine] ORG_CATEGORY_CHAIN resolved - leafOrgId={}, chainSize={}, targetCategoryId={}",
                leaf.getId(),
                chain.size(),
                targetCategoryId
        );

    /* =====================================================
       4) 체인 순서대로 결재선 생성
       ===================================================== */
        List<ApprovalLine> result = new ArrayList<>();
        int orderNo = startOrderNo;

        for (OrgResDto org : chain) {

            // 1️⃣ 조직 엔티티
            Organization organization =
                    orgRepository.getReferenceById(org.getId());

            // 2️⃣ 조직 카테고리에 해당하는 head 직책 찾기
            PositionCategory positionCategory =
                    positionCategoryRepository
                            .findHeadPositionByOrgCategoryId(org.getCategoryId())
                            .orElse(null);

            if (positionCategory == null) {
                log.warn(
                        "[ApprovalLine] ORG_CHAIN no head position - orgId={}, orgCategoryId={}, orderNo={}",
                        org.getId(),
                        org.getCategoryId(),
                        orderNo
                );
            }

            // 3️⃣ 실제 결재자 찾기 (있으면)
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
                        "[ApprovalLine] ORG_CHAIN head employee not found - orgId={}, positionCategoryId={}, orderNo={}",
                        org.getId(),
                        positionCategory == null ? null : positionCategory.getId(),
                        orderNo
                );
            }

            result.add(
                    ApprovalLine.builder()
                            .document(document)
                            .company(company)
                            .organization(organization)
                            .positionCategory(positionCategory)
                            .approver(head) // 없으면 null
                            .orderNo(orderNo++)
                            .status(ApprovalStatus.DRAFT)
                            .build()
            );


            log.debug(
                    "[ApprovalLine] ORG_CHAIN line - orderNo={}, orgId={}, categoryId={}, headEmployeeId={}",
                    (orderNo - 1),
                    org.getId(),
                    org.getCategoryId(),
                    head == null ? null : head.getId()
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
                        .status(ApprovalStatus.DRAFT)
                        .approver(null); // 기본값

        switch (target.getType()) {

            case ORG_AND_POSITION -> {
                // 실제 결재자는 사용자 선택
                // 👉 조직 / 직책 정보만 저장
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
            }

            case FIXED_EMPLOYEE -> {

                // 1️⃣ 규칙 자체 유효성 검사
                if (target.getEmployeeId() == null
                        || target.getOrganizationId() == null
                        || target.getPositionId() == null) {

                    log.warn(
                            "[ApprovalLine] FIXED_EMPLOYEE invalid target - employeeId={}, orgId={}, positionId={}, ruleStep={}",
                            target.getEmployeeId(),
                            target.getOrganizationId(),
                            target.getPositionId(),
                            ruleStep
                    );
                    break;
                }

                // 👉 조직 / 직책 정보는 항상 세팅
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

                Employee employee =
                        employeeRepository
                                .findByIdAndOrgIdAndPositionCategoryIdAndStatus(
                                        target.getEmployeeId(),
                                        target.getOrganizationId(),
                                        target.getPositionId(),
                                        EmployeeStatus.ACTIVE
                                )
                                .orElse(null);

                if (employee != null) {
                    builder.approver(employee);
                } else {
                    log.warn(
                            "[ApprovalLine] FIXED_EMPLOYEE mismatch - employeeId={}, orgId={}, positionId={}, orderNo={}",
                            target.getEmployeeId(),
                            target.getOrganizationId(),
                            target.getPositionId(),
                            orderNo
                    );
                }

            }
        }

        return builder.build();
    }


    public List<ApprovalRuleResDto> getApprovalLinesByDocumentId(Long documentId) {

        if (!documentRepository.existsById(documentId)) {
            throw new NotFoundException("Document not found");
        }

        return approvalLineRepository
                .findByDocument_IdOrderByOrderNoAsc(documentId)
                .stream()
                .map(ApprovalRuleResDto::from)
                .toList();
    }

    @Transactional
    public void updateApprovalLine(Long approvalLineId, Long approvalId) {
        Employee approver = employeeRepository.findById(approvalId)
                .orElseThrow(() -> new NotFoundException("Approver not found"));

        ApprovalLine line = approvalLineRepository.findById(approvalLineId)
                .orElseThrow(() -> new NotFoundException("ApprovalLine not found"));

        line.setApprover(approver);
    }
}