package com.finalproj.orbitflow.approval.logformtemplateai.pipeline.processor;

import com.finalproj.orbitflow.approval.logformtemplateai.dto.AiFormComponent;
import com.finalproj.orbitflow.approval.logformtemplateai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.logformtemplateai.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.logformtemplateai.dto.FormDesignResContext;
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
 * @filename : MetaNormalizeProcessor
 * @since : 26. 1. 8. 목요일
 **/

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