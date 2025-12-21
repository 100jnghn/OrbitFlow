package com.finalproj.orbitflow.attendance.attendanceDefaultRule.dto;

import com.finalproj.orbitflow.attendance.attendanceDefaultRule.entity.AttendanceRule;

import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceRuleResDto
 * @since : 2025. 12. 18. 목요일
 */
public record AttRuleResDto(
        LocalTime defaultStartTime,
        LocalTime defaultEndTime,
        Integer defaultBreakMinutes
) {
    public AttRuleResDto(AttendanceRule rule) {
        this(
                rule.getDefaultStartTime(),
                rule.getDefaultEndTime(),
                rule.getDefaultBreakMinutes()
        );
    }
}