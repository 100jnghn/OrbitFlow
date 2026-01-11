package com.finalproj.orbitflow.schedule.dto;

import lombok.*;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ScheduleSummaryResDto
 * @since : 2025-12-30 오후 9:11 화요일
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSummaryResDto {

    private String dailySummary;
    private String weeklySummary;
}
