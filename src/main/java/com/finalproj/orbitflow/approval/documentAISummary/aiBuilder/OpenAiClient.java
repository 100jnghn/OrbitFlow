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

    @Override
    public String summarize(String prompt) {

        OpenAiChatReqDto request = OpenAiChatReqDto.builder()
                .model(model)
                .temperature(0.3)
                .messages(List.of(
                        new OpenAiChatReqDto.Message(
                                "system",
                                "너는 사내 결재 문서를 요약하는 AI다."
                        ),
                        new OpenAiChatReqDto.Message(
                                "user",
                                prompt
                        )
                ))
                .build();

        OpenAiChatResDto response;
        try {
            response = openAiWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAiChatResDto.class)
                    .block();
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            throw new RuntimeException("AI 요약 요청에 실패했습니다.");
        }

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
            throw new RuntimeException("AI 요약 결과가 비어있습니다.");
        }

        return content.trim();
    }
}