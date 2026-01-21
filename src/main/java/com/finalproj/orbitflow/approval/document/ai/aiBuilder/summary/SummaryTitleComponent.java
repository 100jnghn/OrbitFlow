package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SummaryTitleComponent
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(30)
public class SummaryTitleComponent
        implements PromptComponent<SummaryPromptContext> {

    @Override
    public void append(StringBuilder sb, SummaryPromptContext ctx) {
        sb.append("문서 제목: ")
                .append(ctx.request().getDocumentTitle())
                .append("\n\n");
    }
}
