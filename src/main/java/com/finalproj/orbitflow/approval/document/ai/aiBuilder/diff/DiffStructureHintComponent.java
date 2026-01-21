package com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.aiBuilder.structure.StructureHintBuilder;
import com.finalproj.orbitflow.approval.document.ai.dto.DiffPromptContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 문서 변경 사항(diff) 분석 시 문서 구조 정보를 힌트로 제공하는 컴포넌트.
 * <p>
 * 문서 스키마 정보를 기반으로,
 * 각 필드가 어떤 의미와 구조를 가지는지에 대한 설명을
 * 프롬프트에 추가하여 AI의 이해를 돕는다.
 * <p>
 * 구조 정보는 변경 항목을 추측하기 위한 용도가 아니라,
 * 필드 간 관계와 문서 형태를 올바르게 해석하기 위한
 * 보조 힌트로만 사용된다.
 * <p>
 * StructureHintBuilder를 통해 생성된 구조 설명을 그대로 전달하며,
 * 프롬프트 내에서 문서 구조 정보 영역만을 책임진다.
 *
 * @author : Choi MinHyeok
 * @filename : DiffStructureHintComponent
 * @since : 26. 1. 6. 화요일
 */


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
