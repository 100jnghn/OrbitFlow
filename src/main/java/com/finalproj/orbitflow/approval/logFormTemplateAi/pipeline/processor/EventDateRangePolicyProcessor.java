package com.finalproj.orbitflow.approval.logFormTemplateAi.pipeline.processor;

import com.finalproj.orbitflow.approval.formTemplateGroup.enums.BaseRole;
import com.finalproj.orbitflow.approval.logFormTemplateAi.dto.AiFormComponent;
import com.finalproj.orbitflow.approval.logFormTemplateAi.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.logFormTemplateAi.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.logFormTemplateAi.dto.FormDesignResContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : EventDateRangePolicyProcessor
 * @since : 26. 1. 8. 목요일
 **/


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

            // 1️⃣ baseRole 고정
            meta.put("baseRole", baseRole.name());

            // 2️⃣ affect + ui 정책 결정
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

            // 3️⃣ label 보정 (AI가 비워준 경우 대비)
            String label = c.label();
            if (label == null || label.isBlank()) {
                label = switch (baseRole) {
                    case VACATION -> "휴가 일정";
                    case BUSINESS_TRIP -> "출장 기간";
                    case OUTWORK -> "외근 기간";
                    case COMPANY_EVENT -> "일정";
                };
            }

            // 4️⃣ event-date-range는 항상 필수
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
