package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 결재 문서 요약 프롬프트에 문서 제목 정보를 추가하는 컴포넌트.
 * <p>
 * SummaryPromptContext에 포함된 문서 제목을 프롬프트에 명시적으로 출력하여,
 * AI가 요약 대상 문서를 명확히 인식할 수 있도록 한다.
 * <p>
 * 문서 제목은 요약 결과의 기준점 역할을 하며,
 * 이후에 이어지는 주요 내용 및 참고 정보가
 * 어떤 문서를 설명하는지 혼동되지 않도록 돕는다.
 * <p>
 * SummaryPromptContext를 사용하는 PromptComponent 중 하나로,
 * 프롬프트 내에서 문서 제목 영역만을 책임진다.
 *
 * @author : Choi MinHyeok
 * @filename : SummaryTitleComponent
 * @since : 26. 1. 6. 화요일
 */


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
