package com.finalproj.orbitflow.approval.ai.gateway;

import com.finalproj.orbitflow.openAi.dto.OpenAiChatReqDto;
import com.finalproj.orbitflow.openAi.dto.OpenAiChatResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : OpenAiChatGateway
 * @since : 26. 1. 7. 수요일
 **/


@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiChatGateway {

    private final WebClient openAiWebClient;

    @Value("${openai.model}")
    private String model;

    public String complete(
            String systemPrompt,
            String userPrompt,
            double temperature
    ) {
        OpenAiChatReqDto request = OpenAiChatReqDto.builder()
                .model(model)
                .temperature(temperature)
                .messages(List.of(
                        new OpenAiChatReqDto.Message("system", systemPrompt),
                        new OpenAiChatReqDto.Message("user", userPrompt)
                ))
                .build();

        OpenAiChatResDto response = callOpenAi(request);
        return extractContent(response);
    }

    private OpenAiChatResDto callOpenAi(OpenAiChatReqDto request) {
        try {
            return openAiWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAiChatResDto.class)
                    .block();
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            throw new RuntimeException("OpenAI API 호출에 실패했습니다.");
        }
    }

    private String extractContent(OpenAiChatResDto response) {
        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()
                || response.getChoices().get(0).getMessage() == null) {
            throw new RuntimeException("OpenAI 응답이 비어있습니다.");
        }

        String content = response.getChoices()
                .get(0)
                .getMessage()
                .getContent();

        if (content == null || content.isBlank()) {
            throw new RuntimeException("AI 결과가 비어있습니다.");
        }

        return content.trim();
    }
}
