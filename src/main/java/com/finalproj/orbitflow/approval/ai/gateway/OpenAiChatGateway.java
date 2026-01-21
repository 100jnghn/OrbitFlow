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
 * OpenAI Chat Completion API 연동을 담당하는 Gateway 컴포넌트.
 *
 * <p>
 * 결재 양식 생성 및 AI 기반 문서 처리 기능에서 사용되는
 * OpenAI Chat Completion API 호출을 캡슐화한다.
 * </p>
 *
 * <p>
 * 본 Gateway는 다음 역할만을 책임진다:
 * <ul>
 *   <li>OpenAI 요청 DTO 구성</li>
 *   <li>외부 OpenAI API 호출</li>
 *   <li>응답 검증 및 결과 텍스트 추출</li>
 * </ul>
 * </p>
 *
 * <p>
 * 비즈니스 로직이나 프롬프트 생성 규칙은 포함하지 않으며,
 * AI 모델 선택, 메시지 구성, 예외 처리 등
 * 외부 AI 서비스 연동에 대한 기술적 책임만을 담당한다.
 * </p>
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
