package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.AiSummaryReqDto;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import com.finalproj.orbitflow.approval.form.template.schema.FormTemplateSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiSummaryPromptBuilder
 * @since : 26. 1. 5. 월요일
 **/


@Component
@RequiredArgsConstructor
public class AiSummaryPromptBuilder {

    private final List<PromptComponent<SummaryPromptContext>> components;

    public String build(
            AiSummaryReqDto req,
            FormTemplateSchema schema
    ) {
        SummaryPromptContext ctx =
                new SummaryPromptContext(req, schema);

        StringBuilder sb = new StringBuilder();
        for (PromptComponent<SummaryPromptContext> c : components) {
            c.append(sb, ctx);
        }
        return sb.toString();
    }
}
