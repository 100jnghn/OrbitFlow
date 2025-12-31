package com.finalproj.orbitflow.global.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatbotConfig
 * @since : 2025. 12. 30. 화요일
 */

@Configuration
public class ChatbotConfig {

    @Value("${langchain4j.open-ai.api-key}")
    private String apiKey;

    // 1. 임베딩 모델 빈 등록 (텍스트를 벡터로 변환하는 역할)
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-3-small") // 혹은 text-embedding-ada-002
                .build();
    }

    // 2. 벡터 저장소 빈 등록 (테스트를 위해 우선 메모리 방식 사용)
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }


    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini") // 답변 생성용 모델
                .temperature(0.0)         // 일관된 답변을 위해 0으로 설정
                .build();
    }



}