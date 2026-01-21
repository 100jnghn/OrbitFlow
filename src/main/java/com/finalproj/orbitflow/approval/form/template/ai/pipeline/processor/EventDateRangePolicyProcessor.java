package com.finalproj.orbitflow.approval.form.template.ai.pipeline.processor;

import com.finalproj.orbitflow.approval.form.template.group.enums.BaseRole;
import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormComponent;
import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormDesignResContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI가 생성한 event-date-range 컴포넌트에 대해,
 * 문서의 기본 역할(baseRole)에 맞는 정책을 최종 적용하는 Processor이다.
 * <p>
 * event-date-range는 휴가, 출장, 외근, 회사 일정 등
 * 특정 도메인 문서에서만 사용되는 핵심 컴포넌트이기 때문에,
 * AI가 생성한 결과를 그대로 사용하는 대신
 * 시스템에서 정의한 역할별 정책을 기준으로 보정이 필요하다.
 * <p>
 * 이 Processor는 FormTemplateGroup에 설정된 baseRole을 기준으로
 * 다음과 같은 항목을 일괄적으로 결정한다.
 * <p>
 * - 근태/일정 반영 여부(affect 정책)
 * - UI에서 요구되는 입력 항목(requireTitle, requireReason, requireDescription)
 * - 컴포넌트 라벨 값 보정
 * - event-date-range 컴포넌트의 필수 여부 고정
 * <p>
 * 즉, AI는 "event-date-range를 사용할지 여부"까지만 판단하고,
 * 해당 컴포넌트의 세부 동작과 정책은
 * 이 Processor에서 최종적으로 확정된다.
 * <p>
 * 이를 통해 문서 유형별 동작을 일관되게 유지하고,
 * AI 응답의 편차로 인해 발생할 수 있는 정책 오류를 방지한다.
 *
 * @author Choi MinHyeok
 * @filename EventDateRangePolicyProcessor
 * @since 2026. 1. 8.
 */


@Order(40)
@Component
public class EventDateRangePolicyProcessor implements AiFormResultProcessor {

    @Override
    public AiFormDesignResult process(
            AiFormDesignResult input,
            FormDesignReqContext reqCtx,
            FormDesignResContext resCtx
    ) {
        List<AiFormComponent> comps = input.components();
        if (comps == null || comps.isEmpty()) {
            return input;
        }

        BaseRole baseRole = reqCtx.baseRole(); // ← FormTemplateGroup.baseRole
        if (baseRole == null) {
            return input;
        }

        List<AiFormComponent> out = new ArrayList<>(comps.size());
        boolean applied = false;

        for (AiFormComponent c : comps) {

            if (!"event-date-range".equals(c.type())) {
                out.add(c);
                continue;
            }

            Map<String, Object> meta = new HashMap<>(c.meta());

            meta.put("baseRole", baseRole.name());

            switch (baseRole) {

                case VACATION -> {
                    meta.put("affect", Map.of(
                            "attendance", true,
                            "schedule", true
                    ));
                    meta.put("ui", Map.of(
                            "requireTitle", false,
                            "requireReason", true,
                            "requireDescription", false
                    ));
                }

                case BUSINESS_TRIP, OUTWORK -> {
                    meta.put("affect", Map.of(
                            "attendance", true,
                            "schedule", true
                    ));
                    meta.put("ui", Map.of(
                            "requireTitle", true,
                            "requireReason", false,
                            "requireDescription", true
                    ));
                }

                case COMPANY_EVENT -> {
                    meta.put("affect", Map.of(
                            "attendance", false,
                            "schedule", true
                    ));
                    meta.put("ui", Map.of(
                            "requireTitle", true,
                            "requireReason", false,
                            "requireDescription", true
                    ));
                }
            }

            String label = c.label();
            if (label == null || label.isBlank()) {
                label = switch (baseRole) {
                    case VACATION -> "휴가 일정";
                    case BUSINESS_TRIP -> "출장 기간";
                    case OUTWORK -> "외근 기간";
                    case COMPANY_EVENT -> "일정";
                };
            }

            out.add(new AiFormComponent(
                    c.type(),
                    label,
                    true,
                    meta
            ));

            applied = true;
        }

        if (applied) {
            resCtx.rule("Applied event-date-range policy by templateGroup.baseRole = " + baseRole);
        }

        return new AiFormDesignResult(out);
    }
}
