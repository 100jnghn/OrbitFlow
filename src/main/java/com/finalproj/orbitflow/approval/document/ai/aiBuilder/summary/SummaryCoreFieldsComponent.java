package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.SummaryFieldRenderer;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 결재 문서 요약 프롬프트에 핵심 정보 영역을 추가하는 컴포넌트.
 * <p>
 * SummaryPromptContext에 포함된 문서의 핵심 필드 정보를 기반으로,
 * SummaryFieldRenderer를 사용하여
 * AI가 중요 내용으로 인식해야 할 항목들을 정리된 형태로 출력한다.
 * <p>
 * 본 컴포넌트는 문서 요약에서 반드시 강조되어야 하는
 * 핵심 정보만을 다루며,
 * 부가적인 정보나 첨부 정보는 포함하지 않는다.
 * <p>
 * SummaryPromptContext를 사용하는 PromptComponent 중 하나로,
 * 프롬프트 내에서 "[핵심 정보]" 영역만을 책임진다.
 *
 * @author : Choi MinHyeok
 * @filename : SummaryCoreFieldsComponent
 * @since : 26. 1. 6. 화요일
 */


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
