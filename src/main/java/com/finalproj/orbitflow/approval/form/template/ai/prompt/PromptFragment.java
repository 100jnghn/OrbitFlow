package com.finalproj.orbitflow.approval.form.template.ai.prompt;

/**
 * AI 프롬프트를 구성하는 하나의 조각(Fragment)을 표현하기 위한 인터페이스이다.
 * <p>
 * 전체 프롬프트는 하나의 큰 문자열로 전달되지만,
 * 실제로는 역할 정의, 전역 규칙, 컴포넌트 설명, 사용자 요청 등
 * 성격이 다른 여러 문단이 순서대로 결합된 구조를 가진다.
 * <p>
 * 이 인터페이스는 그러한 프롬프트 구성 요소를
 * 독립적인 단위로 분리하기 위해 정의되었다.
 * <p>
 * 각 구현체는
 * - order(): 프롬프트 내에서의 위치 우선순위
 * - render(): 실제 프롬프트에 포함될 문자열
 * 을 제공한다.
 * <p>
 * 프롬프트의 최종 구성 순서는 order 값을 기준으로 정렬되며,
 * 개별 프래그먼트는 서로의 존재를 알 필요 없이
 * 자신의 내용만 책임지도록 설계되어 있다.
 * <p>
 * 이를 통해 프롬프트 구조를 유연하게 확장하거나
 * 특정 프래그먼트를 교체·추가하는 것이 쉬워진다.
 *
 * @author Choi MinHyeok
 * @filename PromptFragment
 * @since 2026. 1. 7.
 */


public interface PromptFragment {

    int order();

    String render();
}