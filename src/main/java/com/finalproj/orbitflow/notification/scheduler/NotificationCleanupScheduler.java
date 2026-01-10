package com.finalproj.orbitflow.notification.scheduler;

import com.finalproj.orbitflow.notification.service.NotificationCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 누적된 알림을 db에서 지우는 scheduler 클래스
 *
 * @author : 종훈
 * @filename : NotificationCleanupScheduler
 * @since : 2026-01-10 오후 4:51 토요일
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    // 알림 지울 날짜
    @Value("${notification.expire-days}")
    private int expireDays;

    private final NotificationCleanupService notificationCleanupService;

    /**
     * "0 0 2 * * *"
     * 매일 새벽 2시 호출
     */
    @Scheduled(cron = "${notification.cleanup-cron}")
    public void cleanupOldNotifications() {

        log.info("[NotificationCleanup] start");

        try {
            int deletedCount = notificationCleanupService.deleteOldNotifications(expireDays);

            log.info("[NotificationCleanup] end - deleted {} notifications", deletedCount);

        } catch (Exception e) {
            log.error("[NotificationCleanup] failed : ", e);
        }
    }
}
