package com.finalproj.orbitflow.notification.repository;

import com.finalproj.orbitflow.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

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

    List<Notification> findByCompanyIdAndReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long companyId, Long employeeId);
}