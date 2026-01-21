package com.finalproj.orbitflow.schedule.aimodel;

import com.finalproj.orbitflow.openai.dto.OpenAiChatResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * 전달받은 prompt를 사용해서
 * - 일일 일정 요약
 * - 주간 일정 요약
 * 일정 요약 후 반환
 *
 * @author : 종훈
 * @filename : OpenAiSummaryModel
 * @since : 2025-12-30 오후 9:43 화요일
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiSummaryModelService implements SummaryModel {

    private final WebClient openAiWebClient;

    @Value("${openai.model}")
    private String model; // gpt-4o-mini

    private static final float DAILY_TEMPERATURE = 0.3f;
    private static final float WEEKLY_TEMPERATURE = 0.4f;

    private static final int DAILY_MAX_TOKENS = 400;
    private static final int WEEKLY_MAX_TOKENS = 700;

    @Override
    public String summarizeDaily(String prompt) {
        return callGpt(prompt, DAILY_TEMPERATURE, DAILY_MAX_TOKENS);
    }

    @Override
    public String summarizeWeekly(String prompt) {
        return callGpt(prompt, WEEKLY_TEMPERATURE, WEEKLY_MAX_TOKENS);
    }

    private String callGpt(String prompt, float temperature, int maxTokens) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "너는 직장인의 일정 요약 비서다. 아래 입력을 핵심만 간결하게 한국어로 요약해라."
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    ),
                    "temperature", temperature,
                    "max_tokens", maxTokens
            );

            OpenAiChatResDto response = openAiWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(OpenAiChatResDto.class)
                    .block();

            if (response == null
                    || response.getChoices() == null
                    || response.getChoices().isEmpty()
                    || response.getChoices().get(0).getMessage() == null
                    || response.getChoices().get(0).getMessage().getContent() == null) {
                throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
            }

            String result = response.getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            return result;

        } catch (Exception e) {
            throw new RuntimeException("GPT 요약 생성 실패", e);
        }
    }
}
