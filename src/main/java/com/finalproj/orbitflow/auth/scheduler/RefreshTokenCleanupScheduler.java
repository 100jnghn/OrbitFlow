package com.finalproj.orbitflow.auth.scheduler;

import com.finalproj.orbitflow.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RefreshTokenCleanupScheduler
 * @since : 2025-12-18 목요일
 */
@Component
@RequiredArgsConstructor
@EnableScheduling
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    // 매일 새벽 3시
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredRefreshTokens() {
        refreshTokenRepository.deleteExpiredTokens();
    }
}
