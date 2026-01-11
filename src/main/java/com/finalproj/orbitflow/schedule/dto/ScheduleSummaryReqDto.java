package com.finalproj.orbitflow.schedule.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : SummaryReqDto
 * @since : 2025-12-30 오후 8:10 화요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSummaryReqDto {

    // ScheduleResDto에서 '전사' / '개인' / '조직s' 구분 추가
    private String organizationName;

    private String title;
    private String description;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
