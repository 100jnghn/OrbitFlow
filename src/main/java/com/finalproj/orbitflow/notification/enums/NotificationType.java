package com.finalproj.orbitflow.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : NotificationType
 * @since : 2026-01-05 오전 10:15 월요일
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {

    TEST("테스트"),
    SYSTEM("시스템"),
    RESERVATION("예약"),
    SCHEDULE("일정"),
    MESSAGE("메시지"),
    APPROVAL("결재"),
    ATTENDANCE("근태"),
    EMPLOYEE_ORG_CHANGED("조직 변경"),
    EMPLOYEE_POSITION_CHANGED("직책 변경"),
    BOARD("게시판");

    private final String description;
}
