package com.finalproj.orbitflow.notification.controller;

import com.finalproj.orbitflow.global.exception.UnauthorizedException;
import com.finalproj.orbitflow.global.security.jwt.JwtProvider;
import com.finalproj.orbitflow.notification.service.NotificationDeliveryService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 연결
 *
 * @author : 종훈
 * @filename : NotificationController
 * @since : 2026-01-02 오후 4:12 금요일
 */
@RequestMapping("/api/notifications")
@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryController {

    private final NotificationDeliveryService notificationService;
    private final JwtProvider jwtProvider;

    // 사용자 - SSE 연결 생성
    @GetMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter connect(HttpServletRequest request) {

        log.info("SSE : " + "SSE 연결 요청");

        // 토큰 추출
        String sseToken = extractSseToken(request);
        if (sseToken == null) {
            log.error("SSE : sse_token 쿠키 없음");
            throw new UnauthorizedException("SSE 인증 토큰이 없습니다.");
        }

        // 사용자 정보 가져오기
        Long employeeId = jwtProvider.getEmployeeId(sseToken);

        return notificationService.createEmitter(employeeId);
    }

    // 쿠키에서 sse token 추출
    private String extractSseToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("sse_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}