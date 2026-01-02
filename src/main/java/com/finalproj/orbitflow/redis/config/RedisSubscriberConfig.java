package com.finalproj.orbitflow.redis.config;

import com.finalproj.orbitflow.notification.channel.RedisChannels;
import com.finalproj.orbitflow.redis.subscriber.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : RedisSubscriberConfig
 * @since : 2026-01-02 오후 5:32 금요일
 */
@Configuration
@RequiredArgsConstructor
public class RedisSubscriberConfig {

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisSubscriber redisSubscriber;

    /**
     * Redis Pub/Sub 메시지 수신 컨테이너
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        // notification 채널 구독
        container.addMessageListener(
                messageListenerAdapter(),
                new ChannelTopic(RedisChannels.NOTIFICATION)
        );

        return container;
    }

    /**
     * Redis 메시지를 Subscriber의 onMessage 메서드로 위임
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter() {
        return new MessageListenerAdapter(redisSubscriber, "onMessage");
    }
}
