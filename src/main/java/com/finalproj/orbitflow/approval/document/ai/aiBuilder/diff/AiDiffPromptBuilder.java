package com.finalproj.orbitflow.approval.document.ai.aiBuilder.diff;

import com.finalproj.orbitflow.approval.document.ai.aiBuilder.common.PromptComponent;
import com.finalproj.orbitflow.approval.document.ai.dto.DiffPromptContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 문서 변경 사항(diff)을 AI에게 설명하기 위한 프롬프트를 생성하는 빌더 클래스.
 * <p>
 * DiffPromptContext에 담긴 정보들을 기반으로,
 * 여러 PromptComponent 구현체를 순서대로 조합하여
 * 하나의 완성된 프롬프트 문자열을 만든다.
 * <p>
 * 각 PromptComponent는 프롬프트의 특정 역할(시스템 설명, 변경 요약, 출력 규칙 등)을 담당하며,
 * 이 클래스는 해당 컴포넌트들을 조립하는 책임만 가진다.
 * <p>
 * 프롬프트 구성 로직을 컴포넌트 단위로 분리함으로써,
 * 변경 분석 방식이나 출력 포맷이 달라지더라도
 * 개별 컴포넌트 수정만으로 대응할 수 있도록 설계되었다.
 * <p>
 * AI 호출이나 비즈니스 로직은 포함하지 않으며,
 * 순수하게 프롬프트 생성 역할만 담당한다.
 *
 * @author : Choi MinHyeok
 * @filename : AiDiffPromptBuilder
 * @since : 26. 1. 5. 월요일
 */


@Component
@RequiredArgsConstructor
public class AiDiffPromptBuilder {

    private final List<PromptComponent<DiffPromptContext>> components;

    public String build(DiffPromptContext ctx) {
        StringBuilder sb = new StringBuilder();

        for (PromptComponent<DiffPromptContext> component : components) {
            component.append(sb, ctx);
        }

        return sb.toString();
    }
}
