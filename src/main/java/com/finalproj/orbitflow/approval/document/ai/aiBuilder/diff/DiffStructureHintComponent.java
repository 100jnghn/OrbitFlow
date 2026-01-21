package com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.aiBuilder.structure.StructureHintBuilder;
import com.finalproj.orbitflow.approval.document.ai.dto.DiffPromptContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DiffStructureHintComponent
 * @since : 26. 1. 6. 화요일
 **/


@Component
@Order(20)
@RequiredArgsConstructor
public class DiffStructureHintComponent
        implements PromptComponent<DiffPromptContext> {

    private final StructureHintBuilder structureHintBuilder;

    @Override
    public void append(StringBuilder sb, DiffPromptContext ctx) {
        String hint = structureHintBuilder.build(ctx.schema());
        if (hint.isBlank()) return;

        sb.append("""
                [문서 구조 정보]
                """);
        sb.append(hint).append("\n\n");
    }
}
