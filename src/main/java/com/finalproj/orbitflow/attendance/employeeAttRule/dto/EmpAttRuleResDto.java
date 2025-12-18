package com.finalproj.orbitflow.attendance.employeeAttRule.dto;

import com.finalproj.orbitflow.attendance.employeeAttRule.entity.EmployeeAttRule;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : EmpAttRuleResDto
 * @since : 2025. 12. 18. 목요일
 */

// 규칙 응답 DTO (Response)
public record EmpAttRuleResDto(
        Long overrideId,
        Long employeeId,
        String employeeName,
        LocalTime startTime,
        LocalTime endTime,
        Integer breakMinutes,
        String reason,
        LocalDate validFrom,
        LocalDate validTo
) {
    public EmpAttRuleResDto(EmployeeAttRule rule, String employeeName) {
        this(
                rule.getId(),
                rule.getEmployeeId(),
                employeeName,
                rule.getStartTime(),
                rule.getEndTime(),
                rule.getBreakMinutes(),
                rule.getReason(),
                rule.getValidFrom(),
                rule.getValidTo()
        );
    }

}