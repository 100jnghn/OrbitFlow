package com.finalproj.orbitflow.approval.logFormTemplateAi.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproj.orbitflow.approval.logFormTemplateAi.dto.AiFormDesignResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : AiFormResParser
 * @since : 26. 1. 8. 목요일
 **/

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
