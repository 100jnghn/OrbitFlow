package com.finalproj.orbitflow.chatbot.chorma.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChromaClientConfig
 * @since : 2026. 1. 15. 목요일
 */
@Configuration
public class ChromaClientConfig {

    @Bean
    public WebClient chromaWebClient(
            @Value("${chroma.scheme}") String scheme,
            @Value("${chroma.host}") String host,
            @Value("${chroma.port}") int port
    ) {
        String baseUrl = scheme + "://" + host + ":" + port;
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}