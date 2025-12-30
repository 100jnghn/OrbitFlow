package com.finalproj.orbitflow.attendance.exception_rule.dto;

import com.finalproj.orbitflow.attendance.exception_rule.entity.EmployeeAttRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        String employeeNo,
        LocalTime startTime,
        LocalTime endTime,
        Integer breakMinutes,
        String reason,
        LocalDate validFrom,
        LocalDate validTo,
        LocalDateTime appliedAt
) {
    public EmpAttRuleResDto(EmployeeAttRule rule, String employeeName, String employeeNo) {
        this(
                rule.getId(),
                rule.getEmployeeId(),
                employeeName,
                employeeNo,
                rule.getStartTime(),
                rule.getEndTime(),
                rule.getBreakMinutes(),
                rule.getReason(),
                rule.getValidFrom(),
                rule.getValidTo(),
                rule.getAppliedAt()
        );
    }

}