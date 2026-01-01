package com.finalproj.orbitflow.schedule.dto;

import com.finalproj.orbitflow.schedule.enums.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleResDto
 * @since : 2025-12-27 오후 3:29 토요일
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResDto {

    private Long scheduleId;

    private Long companyId;
    private boolean isCompany;
    private boolean isPersonal;

    private Long orgCategoryId;   // null = 개인 일정
    private Long orgId;           // null = 개인 일정

    private Long employeeId;

    private String title;
    private String description;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private ScheduleStatus status;
}