package com.finalproj.orbitflow.notification.repository;

import com.finalproj.orbitflow.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : NotificationRepository
 * @since : 2025-12-16 화요일
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByCompanyIdAndReceiverIdOrderByCreatedAtDesc(Long companyId, Long employeeId);

    List<Notification> findByCompanyIdAndReceiverIdAndIsReadFalseAndCreatedAtAfterOrderByCreatedAtDesc(Long companyId, Long employeeId, Instant thirtyDaysAgo);

    List<Notification> findByReceiverIdAndIsReadFalse(Long employeeId);


    @Modifying(clearAutomatically = true)
    @Query("""
                delete from Notification n
                where n.createdAt < :threshold
            """)
    int deleteByCreatedAtBefore(@Param("threshold") Instant threshold);
}