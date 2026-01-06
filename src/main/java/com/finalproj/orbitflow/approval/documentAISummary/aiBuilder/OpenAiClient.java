package com.finalproj.orbitflow.approval.documentAISummary.aiBuilder;

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
 * @filename : OpenAiClient
 * @since : 26. 1. 5. 월요일
 **/


@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiClient implements AiClient {

    private final WebClient openAiWebClient;

    @Value("${openai.model}")
    private String model;

    private static final String SUMMARY_SYSTEM_PROMPT =
            "너는 사내 결재 문서를 간결하고 명확하게 요약하는 AI다.";

    private static final String DIFF_SYSTEM_PROMPT =
            "너는 사내 결재 문서의 변경 사항을 비교하여 요약하는 AI다.";

    /**
     * 문서 요약
     */
    @Override
    public String summarize(String prompt) {
        OpenAiChatReqDto request = buildRequest(
                SUMMARY_SYSTEM_PROMPT,
                prompt,
                0.3
        );

        OpenAiChatResDto response = callOpenAi(request);
        return extractContent(response);
    }

    /**
     * 문서 비교(DIFF)
     */
    @Override
    public String diff(String prompt) {
        OpenAiChatReqDto request = buildRequest(
                DIFF_SYSTEM_PROMPT,
                prompt,
                0.2
        );

        OpenAiChatResDto response = callOpenAi(request);
        return extractContent(response);
    }

    /**
     * OpenAI ChatCompletion 요청 생성
     */
    private OpenAiChatReqDto buildRequest(
            String systemPrompt,
            String userPrompt,
            double temperature
    ) {
        return OpenAiChatReqDto.builder()
                .model(model)
                .temperature(temperature)
                .messages(List.of(
                        new OpenAiChatReqDto.Message("system", systemPrompt),
                        new OpenAiChatReqDto.Message("user", userPrompt)
                ))
                .build();
    }

    /**
     * OpenAI API 호출
     */
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

    /**
     * 응답에서 content 추출 및 검증
     */
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