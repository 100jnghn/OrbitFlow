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
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : EventDateRangePermissionProcessor
 * @since : 26. 1. 8. 목요일
 **/

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