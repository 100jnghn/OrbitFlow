package com.finalproj.orbitflow.notification.controller;

import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.notification.dto.NotificationResDto;
import com.finalproj.orbitflow.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 알림 CRUD Controller
 *
 * @author : 종훈
 * @filename : NotificationCommandController
 * @since : 2026-01-02 오후 6:24 금요일
 */
@RequestMapping("/api/notifications")
@RestController
@Slf4j
@RequiredArgsConstructor
public class NotificationCommandController {

    private final NotificationCommandService notificationCommandService;

    /**
     * 알림 전체 조회
     */
    @GetMapping
    public ResponseEntity<ResponseDto> getAllNotifications(
            @AuthenticationPrincipal SecurityUser user
    ) {
        List<NotificationResDto> list = notificationCommandService.getAllNotifications(user.getCompanyId(), user.getEmployeeId());

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "알림 전체 조회 성공", list)
        );
    }

    /**
     * 안 읽은 알림 조회
     */
    @GetMapping("/unread")
    public ResponseEntity<ResponseDto> getUnreadNotifications(
            @AuthenticationPrincipal SecurityUser user
    ) {
        List<NotificationResDto> list = notificationCommandService.getUnreadNotifications(user.getCompanyId(), user.getEmployeeId());

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "읽지 않은 알림 조회 성공", list)
        );
    }

    /**
     * 알림 1건 상세 조회
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<ResponseDto> getNotification(
            @PathVariable Long notificationId
    ) {
        NotificationResDto notification = notificationCommandService.getNotification(notificationId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "알림 조회 성공", notification)
        );
    }

    /**
     * 알림 1건 읽음 처리
     */
    @PatchMapping("/{notificationId}")
    public ResponseEntity<ResponseDto> readNotification(
            @PathVariable Long notificationId
    ) {
        notificationCommandService.readNotification(notificationId);

        return ResponseEntity.ok().body(
                new ResponseDto(HttpStatus.OK, "알림 읽음 처리", null)
        );
    }
}
