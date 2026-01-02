package com.finalproj.orbitflow.redis.publisher;

import com.finalproj.orbitflow.notification.channel.RedisChannels;
import com.finalproj.orbitflow.notification.dto.NotificationMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
public class NotificationPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 알림 메시지 발행
     * 이 함수를 호출하여 알림 전송
     */
    public void publish(NotificationMessageDto message) {
        redisTemplate.convertAndSend(
                RedisChannels.NOTIFICATION,
                message
        );

        log.debug("Published notification message: {}", message);
    }
}
