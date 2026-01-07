package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.diff;

import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.documentAISummary.dto.DiffPromptContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiDiffPromptBuilder
 * @since : 26. 1. 5. 월요일
 **/


@Component
@RequiredArgsConstructor
public class AiDiffPromptBuilder {

    private final List<PromptComponent<DiffPromptContext>> components;

    public String build(DiffPromptContext ctx) {
        StringBuilder sb = new StringBuilder();

        for (PromptComponent<DiffPromptContext> component : components) {
            component.append(sb, ctx);
        }

        return sb.toString();
    }
}
