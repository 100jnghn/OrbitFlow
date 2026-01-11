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

    @Value("${chroma.scheme}")
    private String scheme;

    @Value("${chroma.host}")
    private String host;

    @Value("${chroma.port}")
    private int port;

    /**
     * 1️⃣ 임베딩 모델 (텍스트 → 벡터)
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-3-small")
                .build();
    }

    /**
     * 2️⃣ 벡터 저장소 (ChromaDB)
     * - 로컬: http://localhost:8000
     * - K8s : http://chromadb:8000
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        String baseUrl = scheme + "://" + host + ":" + port;

        return ChromaEmbeddingStore.builder()
                .baseUrl(baseUrl)
                .collectionName("orbitflow_manuals")
                .timeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * 3️⃣ 채팅 모델
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .build();
    }
}