package com.finalproj.orbitflow.attendance.commute.dto;

import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : ActiveRuleResDto
 * @since : 2025. 12. 20. 토요일
 */
public record ActiveRuleResDto(
        LocalTime startTime,
        LocalTime endTime
) {
}