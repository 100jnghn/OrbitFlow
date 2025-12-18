package com.finalproj.orbitflow.attendance.attendanceRule.dto;

import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceRuleUpdateReqDto
 * @since : 2025. 12. 18. 목요일
 */

public record AttRuleUpdateReqDto(
        LocalTime defaultStartTime,
        LocalTime defaultEndTime,
        Integer defaultBreakMinutes
) {}
