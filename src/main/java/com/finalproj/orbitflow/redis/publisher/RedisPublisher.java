package com.finalproj.orbitflow.redis.publisher;

import com.finalproj.orbitflow.notification.channel.RedisChannels;
import com.finalproj.orbitflow.notification.dto.NotificationMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : NotificationPublisher
 * @since : 2026-01-02 오후 5:46 금요일
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 알림 메시지 발행
     * 이 함수를 호출하여 알림 전송
     */
    public void publish(NotificationMessageDto dto) {

        try {
            String json = objectMapper.writeValueAsString(dto);

            redisTemplate.convertAndSend(
                    RedisChannels.NOTIFICATION,
                    json
            );

        } catch (Exception e) {
            log.error("Redis publish 실패", e);
        }
    }
}
