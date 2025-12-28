package com.finalproj.orbitflow.schedule.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleReqDto
 * @since : 2025-12-27 오후 5:54 토요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleReqDto {

    private boolean isCompany;

    private Long orgCategoryId;
    private Long orgId;

    private String title;
    private String description;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    /**
     * 기본값: RELEASE
     */
    private String status;
}
