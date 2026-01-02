package com.finalproj.orbitflow.notification.controller;

import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.notification.service.NotificationDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class NotificationDeliveryController {

    private final NotificationDeliveryService notificationService;

    // 사용자 - SSE 연결 생성
    @GetMapping(
            value = "/notifications/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter connect(@AuthenticationPrincipal SecurityUser user) {
        return notificationService.createEmitter(user.getEmployeeId());
    }
}