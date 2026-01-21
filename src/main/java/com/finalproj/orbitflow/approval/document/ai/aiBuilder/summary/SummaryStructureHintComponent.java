package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.aiBuilder.structure.StructureHintBuilder;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 결재 문서 요약 시 문서 구조 정보를 힌트로 제공하는 컴포넌트.
 * <p>
 * 문서 양식 스키마 정보를 기반으로,
 * 각 필드가 어떤 의미와 구조를 가지는지에 대한 설명을
 * 요약 프롬프트에 포함시켜 AI의 이해를 돕는다.
 * <p>
 * 구조 정보는 요약 결과를 직접 생성하기 위한 내용이 아니라,
 * 문서 내용을 올바르게 해석하고
 * 핵심 정보와 참고 정보를 구분하는 데 활용되는 보조 힌트로 사용된다.
 * <p>
 * StructureHintBuilder를 통해 생성된 구조 설명을 그대로 전달하며,
 * 요약 프롬프트 내에서 문서 구조 정보 영역만을 책임진다.
 *
 * @author : Choi MinHyeok
 * @filename : SummaryStructureHintComponent
 * @since : 26. 1. 6. 화요일
 */


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
