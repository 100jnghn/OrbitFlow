package com.finalproj.orbitflow.schedule.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleStatus
 * @since : 2025-12-16 오후 1:15 화요일
 */
@Getter
@RequiredArgsConstructor
public enum ScheduleStatus {
    RELEASE("공개"),
    HOLD("보류"),
    DELETED("삭제됨"),
    ETC("기타");

    private final String description;
}
