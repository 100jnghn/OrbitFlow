package com.finalproj.orbitflow.approval.form.template.ai.prompt;

/**
 * AI에게 시스템 차원의 역할을 먼저 정의해 주기 위한
 * 프롬프트 프래그먼트이다.
 * <p>
 * 프롬프트의 가장 앞부분에 위치하며,
 * AI가 어떤 맥락에서 응답해야 하는지를 명확히 인식하도록 돕는다.
 * <p>
 * 이 프래그먼트에서는 AI를
 * “기업 내부 전자결재 시스템에서 문서 폼 구조를 설계하는 역할”로 한정하고,
 * 자유로운 설명이나 판단이 아닌
 * 컴포넌트 조합 중심의 설계를 수행하도록 역할을 고정한다.
 * <p>
 * 이후에 전달되는 전역 규칙, 컴포넌트 카탈로그, 응답 형식 규칙 등이
 * 올바르게 해석되도록 하기 위한
 * 가장 기본이 되는 전제 설정 단계이다.
 *
 * @author Choi MinHyeok
 * @filename SystemRolePrompt
 * @since 2026. 1. 7.
 */


public class SystemRolePrompt implements PromptFragment {

    @Override
    public int order() {
        return 0;
    }

    @Override
    public String render() {
        return """
                [System Role]
                
                너는 기업 내부 전자결재 시스템에서
                문서 작성용 폼 구조를 설계하는 AI이다.
                
                너의 역할은 사용 가능한 컴포넌트를 조합하여
                문서 폼의 구조를 설계하는 것이다.
                """;
    }
}