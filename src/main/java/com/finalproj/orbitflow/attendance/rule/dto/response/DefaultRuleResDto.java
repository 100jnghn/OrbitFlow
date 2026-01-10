package com.finalproj.orbitflow.attendance.rule.dto.response;

import com.finalproj.orbitflow.attendance.rule.entity.AttendanceRule;

import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceRuleResDto
 * @since : 2025. 12. 18. 목요일
 */
public record DefaultRuleResDto(
        LocalTime defaultStartTime,
        LocalTime defaultEndTime,
        Integer defaultBreakMinutes,
        Integer lateThresholdMin
) {
    public DefaultRuleResDto(AttendanceRule rule) {
        this(
                rule.getDefaultStartTime(),
                rule.getDefaultEndTime(),
                rule.getDefaultBreakMinutes(),
                rule.getLateThresholdMin()
        );
    }
}