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
import com.finalproj.orbitflow.chatbot.manual.entity.ManualMetadata;
import com.finalproj.orbitflow.chatbot.manualCategory.entity.ManualCategory;
import com.finalproj.orbitflow.chatbot.manualCategory.repository.ManualCategoryRepository;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatService
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
        private final com.finalproj.orbitflow.chatbot.manual.repository.ManualRepository manualRepository;

        private final ObjectMapper objectMapper;

        private static final String NO_RESULT =
                "해당 내용은 현재 활성 매뉴얼에서 확인되지 않습니다.";

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
                                                .build());

                return ChatConversationResponseDto.builder()
                                .conversationId(saved.getId())
                                .build();
        }

        @Transactional(readOnly = true)
        public List<ChatConversationListDto> listConversations(Long companyId, Long employeeId) {
                return conversationRepository
                                .findTop20ByCompanyIdAndEmployeeIdAndDeletedFalseOrderByUpdatedAtDesc(companyId,
                                                employeeId)
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

        @Transactional
        public ChatMessageResponseDto sendMessage(Long companyId, Long employeeId, Long conversationId,
                        String content) {

                if (content == null || content.isBlank()) {
                        throw new IllegalArgumentException("content는 필수입니다.");
                }

                ChatConversation conv = conversationRepository
                                .findByIdAndCompanyIdAndEmployeeIdAndDeletedFalse(conversationId, companyId, employeeId)
                                .orElseThrow(() -> new IllegalArgumentException("대화방이 없거나 권한이 없습니다."));

                messageRepository.save(ChatMessage.builder()
                                .companyId(companyId)
                                .conversationId(conv.getId())
                                .role(ChatMessage.Role.USER)
                                .content(content)
                                .metaJson(null)
                                .build());

                Long categoryId = conv.getManualCategoryId();
                String answerText = askQuestion(content, companyId, categoryId);

                if (NO_RESULT.equals(answerText)) {
                    log.info("매뉴얼 미존재 응답 - companyId={}, conversationId={}, categoryId={}",
                            companyId, conversationId, categoryId);
                }

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


        public String askQuestion(String question, Long companyId, Long categoryId) {


                var questionEmbedding = embeddingModel.embed(question).content();

                List<ManualMetadata> activeManuals = (categoryId != null)
                        ? manualRepository.findByCompanyIdAndCategoryIdAndIsActiveTrueOrderByIdDesc(companyId, categoryId)
                        : manualRepository.findAllByCompanyIdAndIsActiveTrueOrderByIdDesc(companyId);


                Set<String> activeFileIdSet = activeManuals.stream()
                                                .map(m -> m.getFile().getId().toString())
                                                .collect(Collectors.toSet());



                String companyIdStr = companyId.toString();
                String categoryIdStr = (categoryId == null ? null : categoryId.toString());

                Filter filter = metadataKey("company_id").isEqualTo(companyIdStr);
                if (categoryIdStr != null) {
                    filter = filter.and(metadataKey("category_id").isEqualTo(categoryIdStr));
                }

                EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                                                .queryEmbedding(questionEmbedding)
                                                .maxResults(50)
                                                .minScore(0.0)
                                                .filter(filter)
                                                .build();


                EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
                List<EmbeddingMatch<TextSegment>> matches = result.matches();

                String context = matches.stream()
                                        .filter(m -> {
                                            var metadata = m.embedded().metadata().toMap();
                                            Object storedFileId = metadata.get("file_id");
                                            return storedFileId != null && activeFileIdSet.contains(storedFileId.toString());
                                        })
                                        .map(m -> m.embedded().text())
                                        .collect(Collectors.joining("\n\n"));

                if (context.isBlank()) {
                    return NO_RESULT;
                }



                if (context.isBlank()) {
                    return "해당 질문에 대한 정보를 매뉴얼에서 찾을 수 없습니다.";
                }

            String prompt = String.format(
                    "중요 규칙:\n" +
                            "- 이전 대화 내용이나 과거에 Assistant가 했던 답변은 사실 근거가 아니다.\n" +
                            "- 오직 아래 [매뉴얼 내용]에 포함된 정보만 근거로 답변해야 한다.\n" +
                            "- [매뉴얼 내용]에 관련 정보가 없거나 비어 있다면,\n" +
                            "  반드시 “해당 내용은 현재 활성 매뉴얼에서 확인되지 않는다”고 답해야 한다.\n" +
                            "- 절대로 기억, 추측, 일반적인 지식으로 답하지 말아야 한다.\n\n" +

                            "너는 우리 회사 인사팀에서 근무하는 직원처럼,\n" +
                            "사내 규정과 매뉴얼을 바탕으로 직원들의 질문을 친절하게 안내하는 AI 도우미다.\n\n" +

                            "아래 [매뉴얼 내용]에 있는 정보만 근거로 답변해야 하며,\n" +
                            "매뉴얼에 없는 내용은 절대 추측하지 말고\n" +
                            "“해당 내용은 현재 매뉴얼에서 확인되지 않는다”고 솔직하게 안내한다.\n" +
                            "이 경우, 답변을 확정하기 위해 필요한 질문 1개를 덧붙여라.\n\n" +

                            "[매뉴얼 내용]\n" +
                            "%s\n\n" +

                            "[사용자 질문]\n" +
                            "%s\n\n" +

                            "[답변 톤 가이드]\n" +
                            "- 인사팀 담당자가 직접 설명해주는 것처럼 부드럽고 친절하게 말한다.\n" +
                            "- 딱딱한 규정 문장 그대로 옮기지 말고, 직원이 이해하기 쉽게 풀어서 설명한다.\n" +
                            "- 불필요하게 길지 않되, “그래서 어떻게 하면 되는지”는 꼭 알려준다.\n" +
                            "- 존댓말을 사용하되 과하게 공손하거나 로봇처럼 보이지 않게 한다.\n" +
                            "- 이모티콘, 특수기호, 과도한 강조 표현은 사용하지 않는다.\n\n" +

                            "[출력 형식]\n" +
                            "먼저, 질문에 대한 답을 한 문장으로 간단히 안내한다.\n\n" +

                            "정리해서 안내드리면\n" +
                            "- (핵심 규정이나 기준을 직원 입장에서 이해하기 쉽게 설명)\n" +
                            "- (적용 대상, 조건, 기간 등 꼭 알아야 할 내용)\n\n" +

                            "이렇게 진행하시면 됩니다\n" +
                            "- (직원이 실제로 해야 할 행동 1)\n" +
                            "- (필요 시 행동 2)\n" +
                            "- (추가 절차가 있다면 간단히)\n\n" +

                            "미리 참고해 주세요\n" +
                            "- (자주 놓치는 부분이나 예외 사항)\n" +
                            "- (오해하기 쉬운 포인트)\n\n" +

                            "매뉴얼 기준으로 보면\n" +
                            "- (근거가 되는 매뉴얼 내용 요약 2~3개)\n\n" +

                            "추가로 확인이 필요하다면\n" +
                            "- (정확한 안내를 위해 직원에게 물어볼 질문 1개)\n\n" +

                            "답변:",

                    context,
                    question
            );


            return chatLanguageModel.generate(prompt);
        }
}