package com.finalproj.orbitflow.approval.line.service;

import com.finalproj.orbitflow.approval.line.dto.ApprovalLineRuleDto;
import com.finalproj.orbitflow.approval.line.dto.ApprovalLineRuleStep;
import com.finalproj.orbitflow.approval.line.dto.RawApprovalRuleStepDto;
import com.finalproj.orbitflow.approval.line.dto.RuleTarget;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalRuleMapper
 * @since : 25. 12. 25. 목요일
 **/


@Component
public class ApprovalRuleMapper {

    public ApprovalLineRuleDto convert(List<RawApprovalRuleStepDto> rawSteps) {

        List<ApprovalLineRuleStep> steps =
                rawSteps.stream()
                        .sorted(Comparator.comparingInt(RawApprovalRuleStepDto::step))
                        .map(this::toRuleStep)
                        .toList();

        return new ApprovalLineRuleDto(steps);
    }

    private ApprovalLineRuleStep toRuleStep(RawApprovalRuleStepDto raw) {

        RuleTarget target = resolveTarget(raw);

        return new ApprovalLineRuleStep(
                raw.step(),
                target
        );
    }

    private RuleTarget resolveTarget(RawApprovalRuleStepDto raw) {

        // 1️⃣ 사원 지정
        if (raw.employeeId() != null) {
            return RuleTarget.fixedEmployee(
                    raw.employeeId(),
                    raw.organizationId(),
                    raw.positionCategoryId()
            );
        }

        // 2️⃣ 조직 + 직책
        if (raw.organizationId() != null && raw.positionCategoryId() != null) {
            return RuleTarget.orgAndPosition(
                    raw.organizationId(),
                    raw.positionCategoryId()
            );
        }

        // 3️⃣ 조직 카테고리 체인
        if (raw.organizationCategoryId() != null) {
            return RuleTarget.orgCategoryChain(
                    raw.organizationCategoryId()
            );
        }

        throw new IllegalArgumentException(
                "결재선 규칙 step=" + raw.step() + " 의 대상이 올바르지 않습니다."
        );
    }
}