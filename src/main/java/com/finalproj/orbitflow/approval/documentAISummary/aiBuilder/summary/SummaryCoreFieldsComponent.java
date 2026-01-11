package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.summary;

import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common.SummaryFieldRenderer;
import com.finalproj.orbitflow.approval.documentAISummary.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SummaryFieldsPart
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(40)
public class SummaryCoreFieldsComponent
        implements PromptComponent<SummaryPromptContext> {

    @Override
    public void append(StringBuilder sb, SummaryPromptContext ctx) {
        SummaryFieldRenderer.appendFields(
                sb,
                "[핵심 정보]",
                ctx.request().getCoreFields()
        );
    }
}
