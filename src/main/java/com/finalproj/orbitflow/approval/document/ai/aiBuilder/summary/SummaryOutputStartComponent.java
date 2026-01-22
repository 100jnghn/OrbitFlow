package com.finalproj.orbitflow.approval.document.ai.aiBuilder.summary;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.SummaryPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 결재 문서 요약 프롬프트에서 실제 출력 결과의 시작 지점을 명시하는 컴포넌트.
 * <p>
 * 프롬프트 상단에 "[요약 결과]" 구분자를 추가하여,
 * 이 지점부터 AI가 최종 요약 내용을 작성해야 함을 명확히 한다.
 * <p>
 * 입력 정보 영역과 출력 결과 영역을 명확히 분리함으로써,
 * AI가 입력 내용을 그대로 복사하지 않고
 * 요약 결과만 생성하도록 유도하는 역할을 한다.
 * <p>
 * SummaryPromptContext를 사용하는 PromptComponent 중 하나로,
 * 프롬프트의 가장 마지막에 배치되어
 * 요약 출력의 시작을 선언하는 용도로 사용된다.
 *
 * @author : Choi MinHyeok
 * @filename : SummaryOutputStartComponent
 * @since : 26. 1. 8. 목요일
 */


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
