package com.finalproj.orbitflow.chatbot.chorma.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChromaVectorServiceImpl
 * @since : 2026. 1. 15. 목요일
 */
@Service
@RequiredArgsConstructor
public class ChromaVectorServiceImpl implements ChromaVectorService {

    private final WebClient chromaWebClient;

    @Value("${chroma.collection}")
    private String collectionName;

    @Override
    public void deleteByFileId(Long companyId, String fileId) {

        Map<String, Object> where = Map.of(
                "$and", List.of(
                        Map.of("company_id", Map.of("$eq", companyId.toString())),
                        Map.of("file_id", Map.of("$eq", fileId))
                )
        );

        Map<String, Object> body = Map.of("where", where);

        chromaWebClient.post()
                .uri("/api/v1/collections/{name}/delete", collectionName)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
