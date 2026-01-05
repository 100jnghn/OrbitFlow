package com.finalproj.orbitflow.chatbot.chatbot.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatbotService
 * @since : 2025. 12. 30. 화요일
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public String askQuestion(String question, Long companyId, Long categoryId) {
        var questionEmbedding = embeddingModel.embed(question).content();

        // 로그 추가: 검색 시작 알림
        log.info("질문 기반 검색 시작: {}", question);

        // 상위 10개로 검색 범위를 넓혀 필터링 후에도 충분한 데이터가 남도록 함
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(questionEmbedding, 20);

        // 로그 추가: 검색된 원본 개수
        log.info("ChromaDB에서 찾은 원본 데이터 수: {}", matches.size());

        String context = matches.stream()
                .filter(match -> {
                    var metadata = match.embedded().metadata().toMap();
                    Object storedCompanyId = metadata.get("company_id");

                    boolean companyMatch = storedCompanyId != null && storedCompanyId.toString().equals(companyId.toString());

                    log.info("필터링 체크 - 저장된ID: {}, 현재ID: {}, 일치여부: {}", storedCompanyId, companyId, companyMatch);

                    return companyMatch;
                })
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        if (context.trim().isEmpty()) {
            log.warn("검색 결과 없음 - 회사ID: {}, 카테고리ID: {}, 질문: {}", companyId, categoryId, question);
            return "해당 질문에 대한 정보를 매뉴얼에서 찾을 수 없습니다.";
        }

        // AI에게 명확한 역할 부여
        String prompt = String.format(
                "당신은 사내 규정 전문가입니다. 아래 [매뉴얼 내용]에 근거하여 답변하세요.\n\n" +
                        "[매뉴얼 내용]:\n%s\n\n" +
                        "[사용자 질문]: %s\n\n" +
                        "답변 (매뉴얼에 내용이 없으면 아는 척하지 말고 모른다고 하세요):", context, question);

        return chatLanguageModel.generate(prompt);
    }
}