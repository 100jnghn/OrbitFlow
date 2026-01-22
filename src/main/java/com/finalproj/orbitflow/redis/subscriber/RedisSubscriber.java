package com.finalproj.orbitflow.redis.subscriber;

import com.finalproj.orbitflow.notification.dto.NotificationMessageDto;
import com.finalproj.orbitflow.notification.service.NotificationDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : RedisSubscriber
 * @since : 2026-01-02 오후 5:34 금요일
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final NotificationDeliveryService notificationService;

    /**
     * Redis Pub/Sub 메시지 수신
     * MessageListenerAdapter에 의해 호출됨
     */
    public void onMessage(String message) {
        try {
            NotificationMessageDto dto = objectMapper.readValue(message, NotificationMessageDto.class);

            // 해당 사용자에게 SSE 전송
            notificationService.send(dto.getEmployeeId(), dto);

        } catch (Exception e) {
            log.error("Redis message 처리 실패: {}", message, e);
        }
    }
}
