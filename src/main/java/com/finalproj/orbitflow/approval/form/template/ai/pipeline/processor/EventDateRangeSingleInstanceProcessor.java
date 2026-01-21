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
 * AI가 하나의 양식에 event-date-range 컴포넌트를
 * 여러 개 생성하는 경우를 방지하기 위한 Processor이다.
 * <p>
 * event-date-range는 일정 생성과 근태/캘린더 연동까지
 * 함께 영향을 주는 핵심 컴포넌트이기 때문에,
 * 하나의 문서에는 반드시 하나만 존재해야 한다는 전제가 있다.
 * <p>
 * 이 Processor는 AI가 생성한 컴포넌트 목록을 순회하면서
 * event-date-range가 두 개 이상 존재할 경우
 * 첫 번째 항목만 유지하고 나머지는 모두 제거한다.
 * <p>
 * 제거가 발생한 경우에는,
 * 후속 단계나 로그 확인 시 판단할 수 있도록
 * 응답 컨텍스트(resCtx)에 적용된 규칙을 기록한다.
 * <p>
 * 이 처리는 AI의 생성 오류를 보정하기 위한 최소한의 안전 장치이며,
 * event-date-range의 단일 사용 원칙을 강제하기 위한 목적을 가진다.
 *
 * @author Choi MinHyeok
 * @filename EventDateRangeSingleInstanceProcessor
 * @since 2026. 1. 8.
 */
@Order(20)
@Component
public class EventDateRangeSingleInstanceProcessor implements AiFormResultProcessor {

    @Override
    public AiFormDesignResult process(
            AiFormDesignResult input,
            FormDesignReqContext reqCtx,
            FormDesignResContext resCtx
    ) {
        List<AiFormComponent> comps = input.components();
        if (comps == null) comps = List.of();

        int count = 0;
        List<AiFormComponent> out = new ArrayList<>();

        for (AiFormComponent c : comps) {
            if ("event-date-range".equals(c.type())) {
                count++;
                if (count > 1) continue;
            }
            out.add(c);
        }

        if (count > 1) {
            resCtx.rule("Kept only one event-date-range (max 1 allowed)");
        }
        return new AiFormDesignResult(out);
    }
}