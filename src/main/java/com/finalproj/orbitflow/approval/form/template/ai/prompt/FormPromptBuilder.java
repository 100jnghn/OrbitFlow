package com.finalproj.orbitflow.approval.form.template.ai.prompt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 여러 프롬프트 프래그먼트를 조합하여
 * AI에게 전달할 최종 프롬프트 문자열을 생성하는 빌더 클래스이다.
 * <p>
 * AI 폼 설계 프롬프트는 하나의 문장이 아니라,
 * 역할 정의, 전역 규칙, 컴포넌트 카탈로그, 응답 형식,
 * 사용자 요청 등 여러 조각으로 구성된다.
 * <p>
 * 이 클래스는 각 프래그먼트를 단계적으로 추가한 뒤,
 * order 값을 기준으로 정렬하여 하나의 프롬프트로 합치는 역할을 한다.
 * <p>
 * base(), components(), responseFormat(), userRequest() 메서드를 통해
 * 필요한 프롬프트 조각만 선택적으로 구성할 수 있으며,
 * 이를 통해 상황에 따라 유연한 프롬프트 조합이 가능하다.
 * <p>
 * build() 메서드는 모든 프래그먼트를 정렬한 후,
 * 사람이 읽기 쉬운 형태의 문자열로 결합하여 반환한다.
 * <p>
 * 프롬프트 구성 순서는 PromptFragment의 order 값에 의해 결정되며,
 * 이 클래스는 순서를 직접 제어하지 않는다.
 *
 * @author Choi MinHyeok
 * @filename FormPromptBuilder
 * @since 2026. 1. 7.
 */


public class FormPromptBuilder {

    private final List<PromptFragment> fragments = new ArrayList<>();

    public FormPromptBuilder base() {
        fragments.add(new SystemRolePrompt());
        fragments.add(new FormGlobalRulesPrompt());
        return this;
    }

    public FormPromptBuilder components() {
        fragments.add(new ComponentCatalogPrompt());
        return this;
    }

    public FormPromptBuilder responseFormat() {
        fragments.add(new FormAiResponseFormatPrompt());
        return this;
    }

    public FormPromptBuilder userRequest(String formName, String purpose, Boolean allowScheduleEvent) {
        fragments.add(new UserRequestPrompt(formName, purpose, allowScheduleEvent));
        return this;
    }


    public String build() {
        return fragments.stream()
                .sorted(Comparator.comparingInt(PromptFragment::order))
                .map(PromptFragment::render)
                .collect(Collectors.joining("\n\n"));
    }
}