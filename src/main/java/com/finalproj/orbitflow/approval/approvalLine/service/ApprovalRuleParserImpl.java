package com.finalproj.orbitflow.approval.approvalLine.service;

import com.finalproj.orbitflow.approval.approvalLine.dto.RawApprovalRuleStepDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApprovalRuleParserImpl
 * @since : 25. 12. 25. 목요일
 **/


@Component
@RequiredArgsConstructor
public class ApprovalRuleParserImpl implements ApprovalRuleParser {

    private final ObjectMapper objectMapper;

    @Override
    public List<RawApprovalRuleStepDto> parse(String approvalRuleJson) {

        if (approvalRuleJson == null || approvalRuleJson.isBlank()) {
            throw new IllegalArgumentException("결재선 규칙 JSON이 비어 있습니다.");
        }

        try {
            List<RawApprovalRuleStepDto> steps =
                    objectMapper.readValue(
                            approvalRuleJson,
                            new TypeReference<List<RawApprovalRuleStepDto>>() {
                            }
                    );

            validate(steps);

            return steps;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "결재선 규칙 JSON 파싱에 실패했습니다.",
                    e
            );
        }
    }

    /**
     * 구조적 최소 검증
     * (비즈니스 규칙은 여기서 하지 않음)
     */
    private void validate(List<RawApprovalRuleStepDto> steps) {

        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("결재선 규칙이 비어 있습니다.");
        }

        Set<Integer> stepSet = new HashSet<>();

        for (RawApprovalRuleStepDto step : steps) {

            if (step.step() <= 0) {
                throw new IllegalArgumentException(
                        "결재 단계(step)는 1 이상이어야 합니다."
                );
            }

            if (!stepSet.add(step.step())) {
                throw new IllegalArgumentException(
                        "중복된 결재 단계(step)가 존재합니다. step=" + step.step()
                );
            }
        }
    }
}