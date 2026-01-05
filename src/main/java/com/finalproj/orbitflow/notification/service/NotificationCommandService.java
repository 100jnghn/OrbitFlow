package com.finalproj.orbitflow.notification.service;

import com.finalproj.orbitflow.hr.company.entity.Company;
import com.finalproj.orbitflow.hr.company.repository.CompanyRepository;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import com.finalproj.orbitflow.notification.dto.NotificationMessageDto;
import com.finalproj.orbitflow.notification.dto.NotificationResDto;
import com.finalproj.orbitflow.notification.entity.Notification;
import com.finalproj.orbitflow.notification.enums.NotificationType;
import com.finalproj.orbitflow.notification.repository.NotificationRepository;
import com.finalproj.orbitflow.redis.publisher.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 생성, 조회
 *
 * @author : 종훈
 * @filename : NotificationCommandService
 * @since : 2026-01-02 오후 6:06 금요일
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationCommandService {

    private final int MAX_SELECT_DATE = 30;

    private final NotificationRepository notificationRepository;
    private final RedisPublisher notificationPublisher;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * 알림 전체 조회
     * 알림 생성일 기준 내림차순
     */
    @Transactional(readOnly = true)
    public List<NotificationResDto> getAllNotifications(Long companyId, Long employeeId) {

        List<Notification> list = notificationRepository.findByCompanyIdAndReceiverIdOrderByCreatedAtDesc(
                companyId,
                employeeId
        );

        return list.stream()
                .map(NotificationResDto::fromEntity)
                .toList();
    }


    /**
     * 읽지 않은 알림 조회
     * 알림 생성일 기준 내림차순
     */
    @Transactional(readOnly = true)
    public List<NotificationResDto> getUnreadNotifications(Long companyId, Long employeeId) {

        // 현재 - 30일 전
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(MAX_SELECT_DATE);

        List<Notification> list = notificationRepository.findByCompanyIdAndReceiverIdAndIsReadFalseAndCreatedAtAfterOrderByCreatedAtDesc(
                companyId,
                employeeId,
                daysAgo
        );

        return list.stream()
                .map(NotificationResDto::fromEntity)
                .toList();
    }

    /**
     * 알림 상세 조회
     */
    @Transactional(readOnly = true)
    public NotificationResDto getNotification(Long notificationId) {

        Notification notification = notificationRepository.getReferenceById(notificationId);

        return NotificationResDto.fromEntity(notification);
    }

    /**
     * 알림 읽음 처리
     * isRead TRUE
     */
    @Transactional
    public void readNotification(Long notificationId) {
        Notification notification = notificationRepository.getReferenceById(notificationId);

        notification.read();
    }

    /**
     * 알림 생성 + 저장 + 발행
     * 다른 서비스에서 호출해서 사용
     */
    @Transactional
    public void createNotification(
            Long companyId,
            Long employeeId,
            NotificationType type,
            String content
    ) {
        Company company = companyRepository.getReferenceById(companyId);
        Employee employee = employeeRepository.getReferenceById(employeeId);

        // Notification 생성
        Notification notification = Notification.builder()
                .company(company)
                .receiver(employee)
                .type(type)
                .content(content)
                .isRead(false)
                .build();

        // RDB에 저장
        notificationRepository.save(notification);

        // dto 변환
        NotificationMessageDto dto = NotificationMessageDto.fromEntity(notification);

        // 알림 publish (발행)
        notificationPublisher.publish(dto);

        log.debug("Notification created & published. id={}", notification.getId());
    }

}
