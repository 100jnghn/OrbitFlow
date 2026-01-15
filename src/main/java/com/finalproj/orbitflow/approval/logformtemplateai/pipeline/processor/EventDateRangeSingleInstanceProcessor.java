package com.finalproj.orbitflow.approval.logformtemplateai.pipeline.processor;

import com.finalproj.orbitflow.approval.logformtemplateai.dto.AiFormComponent;
import com.finalproj.orbitflow.approval.logformtemplateai.dto.AiFormDesignResult;
import com.finalproj.orbitflow.approval.logformtemplateai.dto.FormDesignReqContext;
import com.finalproj.orbitflow.approval.logformtemplateai.dto.FormDesignResContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : EventDateRangeSingleInstanceProcessor
 * @since : 26. 1. 8. 목요일
 **/

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