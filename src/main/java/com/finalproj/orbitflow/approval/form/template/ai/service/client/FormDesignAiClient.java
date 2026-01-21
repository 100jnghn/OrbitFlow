package com.finalproj.orbitflow.approval.form.template.ai.service.client;

import com.finalproj.orbitflow.approval.ai.gateway.OpenAiChatGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AI에게 문서 폼 설계 요청을 전달하기 위한 클라이언트 클래스이다.
 * <p>
 * 이 클래스는 OpenAI Gateway를 직접 호출하지 않고,
 * 폼 설계 전용 진입점을 분리하기 위해 만들어졌다.
 * <p>
 * 역할은 매우 제한적이며,
 * - 폼 설계용 system prompt를 고정해서 전달하고
 * - 외부에서 구성된 사용자 프롬프트를 그대로 AI에 전달한 뒤
 * - AI가 반환한 원본 응답 문자열을 그대로 반환하는 것만 담당한다.
 * <p>
 * 프롬프트 구성, 응답 파싱, 정책 보정, 검증 로직은
 * 모두 상위 서비스 및 파이프라인 계층에서 처리하며,
 * 이 클래스에서는 AI 호출 자체에만 집중한다.
 * <p>
 * system prompt는 AI가 설명 없이
 * 엄격한 JSON 형식만 반환하도록 유도하기 위한 용도로 사용되며,
 * 결과의 안정성을 위해 의도적으로 고정되어 있다.
 *
 * @author Choi MinHyeok
 * @filename FormDesignAiClient
 * @since 2026. 1. 7.
 */


@Component
@RequiredArgsConstructor
public class FormDesignAiClient {

    private final OpenAiChatGateway gateway;

    private static final String FORM_SYSTEM_PROMPT =
            "You are a strict JSON form schema designer. "
                    + "You must follow all rules and output valid JSON only.";

    public String completeFormDesign(String prompt) {
        return gateway.complete(
                FORM_SYSTEM_PROMPT,
                prompt,
                0.1
        );
    }
}
