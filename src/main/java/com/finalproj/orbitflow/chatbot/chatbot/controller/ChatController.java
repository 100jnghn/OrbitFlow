package com.finalproj.orbitflow.chatbot.chatbot.controller;

import com.finalproj.orbitflow.chatbot.chatbot.dto.*;
import com.finalproj.orbitflow.chatbot.chatbot.service.ChatService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatbotController
 * @since : 2025. 12. 30. 화요일
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/chatbot")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * ✅ 기존 단발성 질문 API (호환 유지)
     * - 대화 저장/복원은 하지 않음
     */
    @PostMapping("/ask")
    public ResponseEntity<ResponseDto<String>> askQuestion(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody Map<String, Object> request
    ) {
        String question = (String) request.get("question");

        Long categoryId = null;
        if (request.get("categoryId") != null) {
            Object categoryIdObj = request.get("categoryId");
            if (categoryIdObj instanceof Number) {
                categoryId = ((Number) categoryIdObj).longValue();
            } else if (categoryIdObj instanceof String) {
                categoryId = Long.parseLong((String) categoryIdObj);
            }
        }

        String answer = chatService.askQuestion(question, user.getCompanyId(), categoryId);

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "챗봇 답변 생성 완료", answer)
        );
    }

    /**
     * ✅ (추가) 대화방 생성
     * - 카테고리 선택 후 채팅 시작할 때 호출
     */
    @PostMapping("/conversations")
    public ResponseEntity<ResponseDto<ChatConversationResponseDto>> createConversation(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody ChatConversationRequestDto request
    ) {
        ChatConversationResponseDto res = chatService.createConversation(
                user.getCompanyId(),
                user.getEmployeeId(),
                request.getManualCategoryId()
        );

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "대화방 생성 완료", res)
        );
    }

    /**
     * ✅ (추가) 대화방 목록 조회 (사이드바/복원용)
     */
    @GetMapping("/conversations")
    public ResponseEntity<ResponseDto<List<ChatConversationListDto>>> listConversations(
            @AuthenticationPrincipal SecurityUser user
    ) {
        List<ChatConversationListDto> list = chatService.listConversations(
                user.getCompanyId(),
                user.getEmployeeId()
        );

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "대화방 목록 조회 완료", list)
        );
    }

    /**
     * ✅ (추가) 특정 대화 메시지 조회 (탭 이동 후 복원 핵심)
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ResponseDto<List<ChatMessageDto>>> getMessages(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long conversationId
    ) {
        List<ChatMessageDto> list = chatService.getMessages(
                user.getCompanyId(),
                user.getEmployeeId(),
                conversationId
        );

        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "대화 메시지 조회 완료", list)
        );
    }

    /**
     * ✅ (추가) 질문 전송 (USER 저장 -> 답변 생성 -> ASSISTANT 저장)
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ResponseDto<ChatMessageResponseDto>> sendMessage(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long conversationId,
            @RequestBody ChatMessageRequestDto request
    ) {

        long start = System.currentTimeMillis();

        ChatMessageResponseDto res = chatService.sendMessage(
                user.getCompanyId(),
                user.getEmployeeId(),
                conversationId,
                request.getContent()
        );

        long end = System.currentTimeMillis();
        log.info("[CHAT][CONVERSATION] responseTimeMs={}, companyId={}, conversationId={}",
                end - start,
                user.getCompanyId(),
                conversationId);


        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "챗봇 답변 생성 및 저장 완료", res)
        );
    }
}