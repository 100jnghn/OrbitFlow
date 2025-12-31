package com.finalproj.orbitflow.chatbot.chatbot.controller;

import com.finalproj.orbitflow.chatbot.chatbot.service.ChatService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
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
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<ResponseDto> askQuestion(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody Map<String, String> request) {

        // 1. JSON 바디에서 질문 추출
        String question = request.get("question");

        // 2. 로그인한 사용자의 회사 ID를 기반으로 답변 생성
        // SecurityUser 내부에 getCompanyId() 메서드가 있다면 사용하세요.
        // 없다면 user.getEmployee().getCompany().getId() 형태로 호출해야 합니다.
        String answer = chatService.askQuestion(question, user.getCompanyId());

        // 3. 일관된 응답 형식으로 반환
        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "챗봇 답변 생성 완료", answer)
        );
    }



}
