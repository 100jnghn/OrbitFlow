package com.finalproj.orbitflow.attendance.rule.dto.response;

import com.finalproj.orbitflow.attendance.rule.entity.EmployeeRule;

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
public record EmployeeRuleResDto(
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
    public EmployeeRuleResDto(EmployeeRule rule, String employeeName, String employeeNo) {
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