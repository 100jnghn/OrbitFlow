package com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.DiffPromptContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 문서 변경 사항(diff) 분석을 위한 기본 지침 프롬프트를 구성하는 컴포넌트.
 * <p>
 * AI가 어떤 역할로 문서를 비교해야 하는지,
 * 그리고 어떤 기준과 규칙을 따라 변경 사항을 요약해야 하는지를
 * 프롬프트의 가장 앞부분에서 명확히 정의한다.
 * <p>
 * 변경 요약의 범위, 표현 방식, 언어, 분량 등의 기본 규칙을 고정하여
 * 이후에 이어지는 diff 관련 컴포넌트들의 출력 결과가
 * 일관된 기준 아래 해석되도록 하는 역할을 한다.
 * <p>
 * DiffPromptContext를 사용하는 PromptComponent 중
 * 가장 앞단에 위치하는 기본 컴포넌트로 사용된다.
 *
 * @author : Choi MinHyeok
 * @filename : DiffBasePromptComponent
 * @since : 26. 1. 6. 화요일
 */


@Component
@Order(10)
public class DiffBasePromptComponent
        implements PromptComponent<DiffPromptContext> {

    @Override
    public void append(StringBuilder sb, DiffPromptContext ctx) {
        sb.append("""
                너는 사내 결재 문서를 검토·비교하는 AI다.
                아래 정보는 동일한 양식으로 작성된 두 문서를 비교하기 위해
                서버에서 정규화한 결과이다.
                
                이전 문서와 현재 문서를 비교하여
                변경된 사항의 핵심이 잘 드러나도록 요약하라.
                
                규칙:
                - 변경된 항목만 서술할 것
                - 변경 전 → 변경 후 형태로 설명할 것
                - 변경이 없는 항목은 언급하지 말 것
                - 단순한 표현 변경이나 형식 차이는 중요도가 낮다
                - 문서 제목이 변경된 경우에만 변경 사항에 포함한다
                - 추측하지 말고 제공된 정보만 사용할 것
                - 결과는 한국어로 작성할 것
                - 3~6문장 이내의 보고용 문장으로 작성할 것
                
                """);
    }
}
