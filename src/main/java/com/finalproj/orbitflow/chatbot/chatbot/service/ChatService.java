package com.finalproj.orbitflow.chatbot.chatbot.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
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
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatLanguageModel chatLanguageModel; // ChatbotConfig에 추가 필요
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public String askQuestion(String question, Long companyId) {
        // 1. 질문을 벡터로 변환 (Embedding)
        var questionEmbedding = embeddingModel.embed(question).content();

        // 2. 벡터 DB에서 가장 유사한 매뉴얼 조각 3개 검색 (Retrieval)
        // 실제 운영 시에는 metadata 필터링(company_id 등)을 추가하는 것이 좋습니다.
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(questionEmbedding, 3);

        // 3. 검색된 내용을 하나의 컨텍스트로 합침
        String context = relevant.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        // 4. 프롬프트 생성 및 LLM 답변 요청 (Generation)
        String prompt = String.format(
                "당신은 회사의 매뉴얼 전문가입니다. 아래 제공된 [매뉴얼 내용]을 바탕으로 사용자의 [질문]에 친절하게 답하세요.\n\n" +
                        "[매뉴얼 내용]:\n%s\n\n" +
                        "[질문]:\n%s\n\n" +
                        "정답을 모를 경우 '매뉴얼에 해당 내용이 없습니다'라고 답하세요.",
                context, question);

        return chatLanguageModel.generate(prompt);
    }
}