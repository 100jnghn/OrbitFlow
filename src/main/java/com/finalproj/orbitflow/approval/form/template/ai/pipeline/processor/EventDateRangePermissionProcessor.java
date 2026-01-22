package com.finalproj.orbitflow.approval.form.template.ai.pipeline.processor;

import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormComponent;
import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.form.template.ai.dto.FormDesignResContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI가 생성한 양식 설계 결과 중,
 * 일정(event-date-range) 컴포넌트의 사용 가능 여부를 검증하고
 * 허용되지 않은 경우 이를 제거하는 역할을 담당한다.
 * <p>
 * 일부 문서 유형에서는 일정 생성이나 근태/캘린더 연동이
 * 정책상 허용되지 않기 때문에,
 * AI가 event-date-range 컴포넌트를 생성하더라도
 * 그대로 사용할 수 없는 경우가 발생한다.
 * <p>
 * 이 Processor는 요청 컨텍스트에 포함된
 * 일정 이벤트 허용 여부 설정을 기준으로,
 * event-date-range 컴포넌트를 최종 결과에서 제거한다.
 * <p>
 * 컴포넌트가 제거된 경우에는,
 * 이후 단계에서 확인할 수 있도록
 * 응답 컨텍스트(resCtx)에 제거 사유를 기록한다.
 * <p>
 * 이 처리는 AI의 판단을 무시하기 위한 것이 아니라,
 * 시스템 정책을 최종 기준으로 삼기 위한 안전 장치 역할을 한다.
 *
 * @author Choi MinHyeok
 * @filename EventDateRangePermissionProcessor
 * @since 2026. 1. 8.
 */
@Order(10)
@Component
public class EventDateRangePermissionProcessor implements AiFormResultProcessor {

    @Override
    public AiFormDesignResult process(
            AiFormDesignResult input,
            FormDesignReqContext reqCtx,
            FormDesignResContext resCtx
    ) {
        List<AiFormComponent> comps = input.components();
        if (comps == null) comps = List.of();

        if (reqCtx.allowScheduleEvent()) return new AiFormDesignResult(comps);

        List<AiFormComponent> filtered = new ArrayList<>();
        boolean removed = false;

        for (AiFormComponent c : comps) {
            if ("event-date-range".equals(c.type())) {
                removed = true;
                continue;
            }
            filtered.add(c);
        }

        if (removed) {
            resCtx.rule("Removed event-date-range (schedule event not allowed)");
        }
        return new AiFormDesignResult(filtered);
    }
}