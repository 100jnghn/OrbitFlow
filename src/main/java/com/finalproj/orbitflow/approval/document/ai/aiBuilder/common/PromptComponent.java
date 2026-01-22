package com.finalproj.orbitflow.approval.document.ai.aiBuilder.common;

/**
 * AI 프롬프트 생성을 구성 요소 단위로 분리하기 위한 공통 인터페이스.
 *
 * <p>
 * 하나의 {@code PromptComponent}는 전체 AI 프롬프트를 구성하는
 * 부분적인 문장 또는 규칙을 담당하며,
 * 주어진 컨텍스트를 기반으로 프롬프트 내용을 문자열로 누적한다.
 * </p>
 *
 * <p>
 * 여러 {@code PromptComponent} 구현체는 조합되어 하나의 완성된 프롬프트를 구성하며,
 * 이를 통해 프롬프트 생성 로직을 모듈화하고 재사용성을 높인다.
 * </p>
 *
 * <p>
 * 본 인터페이스는 AI 모델, 호출 방식, 비즈니스 로직과는 독립적으로 설계되었으며,
 * 프롬프트 생성 규칙만을 책임지는 순수한 빌더 구성 요소로 사용된다.
 * </p>
 *
 * @param <T> 프롬프트 생성에 필요한 컨텍스트 타입
 * @author Choi MinHyeok
 * @since 2026.01.06
 */


public interface PromptComponent<T> {
    void append(StringBuilder sb, T context);
}
