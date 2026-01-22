package com.finalproj.orbitflow.approval.form.template.ai.pipeline.processor;

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
 * AI가 생성한 컴포넌트 결과에서 meta 정보와 required 값이
 * null로 들어오는 경우를 정리하기 위한 보정 Processor이다.
 * <p>
 * AI 응답 특성상 일부 컴포넌트는 meta가 아예 없거나,
 * required 값이 명시되지 않은 상태로 내려오는 경우가 있다.
 * 이 상태를 그대로 두면 이후 단계에서 NPE가 발생하거나
 * 검증 로직이 불필요하게 복잡해질 수 있다.
 * <p>
 * 이 Processor에서는 의미 판단이나 정책 검증은 수행하지 않고,
 * 단순히 다음 단계에서 안전하게 처리할 수 있도록
 * 기본 형태만 맞춰주는 역할만 담당한다.
 * <p>
 * - meta가 null인 경우 빈 Map으로 보정
 * - required가 null인 경우 false로 보정
 * <p>
 * divider, notice처럼 required 개념이 없는 컴포넌트에 대한
 * 의미적 검증은 여기서 하지 않으며,
 * 해당 부분은 이후 전용 Processor에서 처리한다.
 * <p>
 * 즉, 이 단계는 "정책 적용"이 아니라
 * "데이터 형태 안정화"를 위한 전처리 단계이다.
 *
 * @author Choi MinHyeok
 * @filename MetaNormalizeProcessor
 * @since 2026. 1. 8.
 */


@Order(30)
@Component
public class MetaNormalizeProcessor implements AiFormResultProcessor {

    @Override
    public AiFormDesignResult process(
            AiFormDesignResult input,
            FormDesignReqContext reqCtx,
            FormDesignResContext resCtx
    ) {
        List<AiFormComponent> comps = input.components();
        if (comps == null) comps = List.of();

        List<AiFormComponent> out = new ArrayList<>(comps.size());
        boolean fixedMeta = false;
        boolean fixedRequired = false;

        for (AiFormComponent c : comps) {
            Map<String, Object> meta = c.meta() == null ? new HashMap<>() : c.meta();
            Boolean required = c.required();

            // divider/notice는 required가 없다는 규칙이 있지만,
            // 여기서는 "null 들어오면 false로 보정"만 하고, 검증은 다음 단계에서.
            if (required == null) {
                required = false;
                fixedRequired = true;
            }

            if (c.meta() == null) fixedMeta = true;

            out.add(new AiFormComponent(c.type(), c.label(), required, meta));
        }

        if (fixedMeta) resCtx.rule("Normalized meta: null -> {}");
        if (fixedRequired) resCtx.warn("Normalized required: null -> false (needs later validation)");

        return new AiFormDesignResult(out);
    }
}