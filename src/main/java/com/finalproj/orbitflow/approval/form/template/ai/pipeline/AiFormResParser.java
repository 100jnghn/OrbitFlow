package com.finalproj.orbitflow.approval.form.template.ai.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproj.orbitflow.approval.form.template.ai.dto.AiFormDesignResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AI가 반환한 원본 JSON 문자열을
 * 양식 설계 결과 객체로 변환하기 위한 파서 클래스이다.
 * <p>
 * AI 응답은 문자열 형태의 JSON으로 전달되기 때문에,
 * 이후 파이프라인에서 사용하기 위해서는
 * 내부 DTO 구조로 한 번 변환하는 과정이 필요하다.
 * <p>
 * 이 클래스는 AI 응답을 AiFormDesignResult 형태로 파싱하는
 * 단일 책임만을 가지며,
 * 파싱 실패 시에는 이후 처리가 불가능하므로 예외를 그대로 전달한다.
 * <p>
 * JSON 구조의 유효성 검증이나 정책 판단은
 * 이 단계에서 수행하지 않으며,
 * 해당 책임은 이후 Processor 단계에서 처리된다.
 *
 * @author Choi MinHyeok
 * @filename AiFormResParser
 * @since 2026. 1. 8.
 */


@Component
@RequiredArgsConstructor
public class AiFormResParser {
    private final ObjectMapper objectMapper;

    public AiFormDesignResult parse(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, AiFormDesignResult.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 응답 JSON 파싱 실패", e);
        }
    }
}
