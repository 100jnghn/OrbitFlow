package com.finalproj.orbitflow.attendance.attendanceExceptionRule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : EmpAttRuleCreateReqDto
 * @since : 2025. 12. 18. 목요일
 */
// 규칙 생성 요청 DTO (POST /exception)
public record EmpAttRuleCreateReqDto(
        Long employeeId,
        LocalTime startTime,
        LocalTime endTime,
        Integer breakMinutes,
        String reason,
        LocalDate validFrom,
        LocalDate validTo
) { }