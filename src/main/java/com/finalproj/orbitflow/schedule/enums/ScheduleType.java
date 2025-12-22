package com.finalproj.orbitflow.schedule.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleType
 * @since : 2025-12-16 오후 1:13 화요일
 */
@Getter
@RequiredArgsConstructor
public enum ScheduleType {
    COMPANY("전사일정"),
    PERSONAL("개인일정"),
    ORGANIZATION("조직일정"),
    ETC("기타");

    private final String description;
}
