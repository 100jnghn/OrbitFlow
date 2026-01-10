package com.finalproj.orbitflow.notification.service;

import com.finalproj.orbitflow.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : NotificationCleanupSerevice
 * @since : 2026-01-10 오후 4:52 토요일
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationCleanupService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public int deleteOldNotifications(int days) {

        // 오늘에서 days 이전 날짜
        Instant threshold = Instant.now().minus(days, ChronoUnit.DAYS);
        return notificationRepository.deleteByCreatedAtBefore(threshold);
    }
}
