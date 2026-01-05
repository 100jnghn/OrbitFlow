package com.finalproj.orbitflow.global.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

/**
 * @author : rlagkdus
 * @filename : ChatbotConfig
 * @since : 2025. 12. 30. 화요일
 */
@Configuration
public class ChatbotConfig {

    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    // 1. 임베딩 모델 빈 등록 (텍스트를 벡터로 변환)
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-3-small")
                .build();
    }

    // 2. 벡터 저장소 빈 등록 (ChromaDB 연동)
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl("http://localhost:8000")
                .collectionName("orbitflow_manuals")
                .timeout(Duration.ofSeconds(30)) // 연결 시간 초과 방지 추가
                .build();
    }

    // 3. 채팅 언어 모델 빈 등록
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini") // 가성비 좋은 모델로 설정
                .temperature(0.0)         // 매뉴얼 기반 답변이므로 창의성보다는 정확도 위주
                .build();
    }
}