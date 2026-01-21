package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SummaryOutputStartComponent
 * @since : 26. 1. 8. 목요일
 **/


@Component
@Order(1000)
public class SummaryOutputStartComponent
        implements PromptComponent<SummaryPromptContext> {

    @Override
    public void append(StringBuilder sb, SummaryPromptContext ctx) {
        sb.append("""
                
                [요약 결과]
                """);
    }
}
