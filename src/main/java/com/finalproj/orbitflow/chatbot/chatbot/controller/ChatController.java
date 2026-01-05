package com.finalproj.orbitflow.chatbot.chatbot.controller;

import com.finalproj.orbitflow.chatbot.chatbot.service.ChatService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ChatbotController
 * @since : 2025. 12. 30. 화요일
 */

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Profile("chatbot")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<ResponseDto<String>> askQuestion(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody Map<String, Object> request) {

        // 1. JSON 바디에서 질문과 카테고리 ID 추출
        String question = (String) request.get("question");
        Long categoryId = null;
        if (request.get("categoryId") != null) {
            // Integer나 Long으로 올 수 있으므로 변환 처리
            Object categoryIdObj = request.get("categoryId");
            if (categoryIdObj instanceof Number) {
                categoryId = ((Number) categoryIdObj).longValue();
            } else if (categoryIdObj instanceof String) {
                categoryId = Long.parseLong((String) categoryIdObj);
            }
        }

        // 2. 로그인한 사용자의 회사 ID를 기반으로 답변 생성
        String answer = chatService.askQuestion(question, user.getCompanyId(), categoryId);

        // 3. 일관된 응답 형식으로 반환
        return ResponseEntity.ok(
                new ResponseDto<>(HttpStatus.OK, "챗봇 답변 생성 완료", answer)
        );
    }



}
