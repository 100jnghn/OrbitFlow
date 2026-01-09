package com.finalproj.orbitflow.chatbot.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproj.orbitflow.chatbot.chatbot.dto.ChatConversationListDto;
import com.finalproj.orbitflow.chatbot.chatbot.dto.ChatConversationResponseDto;
import com.finalproj.orbitflow.chatbot.chatbot.dto.ChatMessageDto;
import com.finalproj.orbitflow.chatbot.chatbot.dto.ChatMessageResponseDto;
import com.finalproj.orbitflow.chatbot.chatbot.entity.ChatConversation;
import com.finalproj.orbitflow.chatbot.chatbot.entity.ChatMessage;
import com.finalproj.orbitflow.chatbot.chatbot.repository.ChatConversationRepository;
import com.finalproj.orbitflow.chatbot.chatbot.repository.ChatMessageRepository;
import com.finalproj.orbitflow.chatbot.manualCategory.entity.ManualCategory;
import com.finalproj.orbitflow.chatbot.manualCategory.repository.ManualCategoryRepository;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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

    // === 기존 RAG 의존성 ===
    private final ChatLanguageModel chatLanguageModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    // === 추가: 대화 저장용 ===
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ManualCategoryRepository manualCategoryRepository;

    private final ObjectMapper objectMapper;

    /**
     * ✅ (추가) 대화방 생성
     * - manualCategoryId는 null 허용 가능 (정책에 맞게)
     */
    @Transactional
    public ChatConversationResponseDto createConversation(Long companyId, Long employeeId, Long manualCategoryId) {

        Long categoryId = null;
        String categoryNameSnapshot = null;

        if (manualCategoryId != null) {
            ManualCategory category = manualCategoryRepository
                    .findByIdAndCompanyIdAndIsActiveTrue(manualCategoryId, companyId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 매뉴얼 카테고리입니다."));

            categoryId = category.getId();
            categoryNameSnapshot = category.getCategoryName(); // 컬럼명/게터는 프로젝트에 맞게
        }

        ChatConversation saved = conversationRepository.save(
                ChatConversation.builder()
                        .companyId(companyId)
                        .employeeId(employeeId)
                        .manualCategoryId(categoryId)
                        .manualCategoryName(categoryNameSnapshot)
                        .status(ChatConversation.Status.ACTIVE)
                        .deleted(false)
                        .build()
        );

        return ChatConversationResponseDto.builder()
                .conversationId(saved.getId())
                .build();
    }

    /**
     * ✅ (추가) 대화 목록
     */
    @Transactional(readOnly = true)
    public List<ChatConversationListDto> listConversations(Long companyId, Long employeeId) {
        return conversationRepository
                .findTop20ByCompanyIdAndEmployeeIdAndDeletedFalseOrderByUpdatedAtDesc(companyId, employeeId)
                .stream()
                .map(c -> ChatConversationListDto.builder()
                        .conversationId(c.getId())
                        .manualCategoryId(c.getManualCategoryId())
                        .manualCategoryName(c.getManualCategoryName())
                        .title(c.getTitle())
                        .status(c.getStatus().name())
                        .updatedAt(c.getUpdatedAt()) // BaseEntity(Instant)
                        .build())
                .toList();
    }

    /**
     * ✅ (추가) 특정 대화 메시지(복원)
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(Long companyId, Long employeeId, Long conversationId) {
        ChatConversation conv = conversationRepository
                .findByIdAndCompanyIdAndEmployeeIdAndDeletedFalse(conversationId, companyId, employeeId)
                .orElseThrow(() -> new IllegalArgumentException("대화방이 없거나 권한이 없습니다."));

        return messageRepository.findByConversationIdOrderByCreatedAtAscIdAsc(conv.getId())
                .stream()
                .map(m -> ChatMessageDto.builder()
                        .messageId(m.getId())
                        .role(m.getRole().name())
                        .content(m.getContent())
                        .meta(m.getMetaJson())
                        .createdAt(m.getCreatedAt()) // BaseEntity(Instant)
                        .build())
                .toList();
    }

    /**
     * ✅ (추가) 질문 전송: USER 저장 -> RAG 답변 생성 -> ASSISTANT 저장 -> 응답 DTO
     */
    @Transactional
    public ChatMessageResponseDto sendMessage(Long companyId, Long employeeId, Long conversationId, String content) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content는 필수입니다.");
        }

        ChatConversation conv = conversationRepository
                .findByIdAndCompanyIdAndEmployeeIdAndDeletedFalse(conversationId, companyId, employeeId)
                .orElseThrow(() -> new IllegalArgumentException("대화방이 없거나 권한이 없습니다."));

        // 1) USER 저장
        messageRepository.save(ChatMessage.builder()
                .companyId(companyId)
                .conversationId(conv.getId())
                .role(ChatMessage.Role.USER)
                .content(content)
                .metaJson(null)
                .build());

        // 2) 답변 생성 (기존 askQuestion 활용)
        Long categoryId = conv.getManualCategoryId();
        String answerText = askQuestion(content, companyId, categoryId);

        // 3) ASSISTANT meta 구성(필요시 확장)
        HashMap<String, Object> metaMap = new HashMap<>();
        metaMap.put("companyId", companyId);
        metaMap.put("categoryId", categoryId);
        JsonNode metaJson = objectMapper.valueToTree(metaMap);

        ChatMessage savedAssistant = messageRepository.save(ChatMessage.builder()
                .companyId(companyId)
                .conversationId(conv.getId())
                .role(ChatMessage.Role.ASSISTANT)
                .content(answerText)
                .metaJson(metaJson)
                .build());

        // 4) 대화 title 세팅(옵션)
        conv.setTitleIfEmpty(trimTo255(content));

        return ChatMessageResponseDto.builder()
                .assistant(ChatMessageDto.builder()
                        .messageId(savedAssistant.getId())
                        .role(savedAssistant.getRole().name())
                        .content(savedAssistant.getContent())
                        .meta(savedAssistant.getMetaJson())
                        .createdAt(savedAssistant.getCreatedAt())
                        .build())
                .build();
    }

    private String trimTo255(String s) {
        String t = s.trim();
        return t.length() > 255 ? t.substring(0, 255) : t;
    }

    /**
     * ✅ 기존 메서드 개선: company + category 둘 다 필터링 적용
     * (기존 코드에서는 categoryId를 읽기만 하고 실제 필터는 company만 적용 중이었음) :contentReference[oaicite:2]{index=2}
     */
    public String askQuestion(String question, Long companyId, Long categoryId) {
        var questionEmbedding = embeddingModel.embed(question).content();

        log.info("질문 기반 검색 시작: {}", question);

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(questionEmbedding, 20);
        log.info("ChromaDB에서 찾은 원본 데이터 수: {}", matches.size());

        String context = matches.stream()
                .filter(match -> {
                    var metadata = match.embedded().metadata().toMap();
                    Object storedCompanyId = metadata.get("company_id");
                    Object storedCategoryId = metadata.get("category_id");

                    boolean companyMatch = storedCompanyId != null
                            && storedCompanyId.toString().equals(companyId.toString());

                    boolean categoryMatch = (categoryId == null) // 정책: null이면 회사 전체 검색
                            || (storedCategoryId != null && storedCategoryId.toString().equals(categoryId.toString()));

                    log.info("필터링 체크 - companyMatch={}, categoryMatch={}, storedCompanyId={}, storedCategoryId={}",
                            companyMatch, categoryMatch, storedCompanyId, storedCategoryId);

                    return companyMatch && categoryMatch;
                })
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        if (context.trim().isEmpty()) {
            log.warn("검색 결과 없음 - 회사ID: {}, 카테고리ID: {}, 질문: {}", companyId, categoryId, question);
            return "해당 질문에 대한 정보를 매뉴얼에서 찾을 수 없습니다.";
        }

        String prompt = String.format(
                "당신은 사내 규정 전문가입니다. 아래 [매뉴얼 내용]에 근거하여 사용자의 질문에 답변하세요.\n\n" +
                        "### [매뉴얼 내용]\n" +
                        "%s\n\n" +
                        "### [사용자 질문]\n" +
                        "%s\n\n" +
                        "### [답변 작성 규칙]\n" +
                        "1. 이모티콘이나 특수 기호를 절대 사용하지 마세요.\n" +
                        "2. 각 섹션([핵심 요약], [상세 안내], [주의 사항]) 사이에는 반드시 빈 줄을 두 번 삽입(줄바꿈 2번)하여 간격을 넓게 두세요.\n" +
                        "3. 모든 내용은 항목별(Bullet point)로 간결하게 작성하세요.\n" +
                        "4. 매뉴얼에 없는 내용은 지어내지 마세요.\n\n" +
                        "### [출력 양식]\n" +
                        "[핵심 요약]\n" +
                        "(질문에 대한 한 문장 결론)\n" +
                        "[상세 안내]\n" +
                        "- (상세 규정 1)\n" +
                        "- (상세 규정 2)\n" +
                        "[주의 사항]\n" +
                        "- (유의사항 및 예외 조항)\n" +
                        "--- \n" +
                        "### 답변:",
                context, question
        );

        return chatLanguageModel.generate(prompt);
    }
}