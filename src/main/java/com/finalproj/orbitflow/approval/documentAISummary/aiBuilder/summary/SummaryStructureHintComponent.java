package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.summary;

import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.documentAISummary.aiBuilder.structure.StructureHintBuilder;
import com.finalproj.orbitflow.approval.documentAISummary.dto.SummaryPromptContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : SummaryStructureHintComponent
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(20)
@RequiredArgsConstructor
public class SummaryStructureHintComponent
        implements PromptComponent<SummaryPromptContext> {

    private final StructureHintBuilder structureHintBuilder;

    @Override
    public void append(StringBuilder sb, SummaryPromptContext ctx) {
        String hint = structureHintBuilder.build(ctx.schema());
        if (hint.isBlank()) return;

        sb.append("""
                [문서 구조 정보]
                """);
        sb.append(hint).append("\n\n");
    }
}
