package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.SummaryFieldRenderer;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SummaryOptionalFieldsComponent
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(50)
public class SummaryOptionalFieldsComponent
        implements PromptComponent<SummaryPromptContext> {

    @Override
    public void append(StringBuilder sb, SummaryPromptContext ctx) {
        var dto = ctx.request();

        if (dto.getOptionalFields() == null ||
                dto.getOptionalFields().isEmpty()) {
            return;
        }

        sb.append("\n");

        SummaryFieldRenderer.appendFields(
                sb,
                "[추가 정보]",
                dto.getOptionalFields()
        );
    }
}
