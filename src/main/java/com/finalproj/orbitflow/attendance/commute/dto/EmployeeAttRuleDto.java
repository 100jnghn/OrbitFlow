package com.finalproj.orbitflow.attendance.commute.dto;

import com.finalproj.orbitflow.attendance.commute.entity.EmployeeAttRule;

import java.time.LocalDate;
import java.time.LocalTime;

public class EmployeeAttRuleDto {


    // 규칙 생성 요청 DTO (POST /exception)
    public record EmployeeAttRuleCreateRequest(
            Long employeeId,
            LocalTime startTime,
            LocalTime endTime,
            Integer breakMinutes,
            String reason,
            LocalDate validFrom,
            LocalDate validTo
    ) {
    }

    // 규칙 수정 요청 DTO (PUT /exception/{ruleId})
    public record EmployeeAttRuleUpdateRequest(
            LocalTime startTime,
            LocalTime endTime,
            Integer breakMinutes,
            String reason,
            LocalDate validFrom,
            LocalDate validTo,
            Boolean isActive
    ) {
    }

    // 규칙 응답 DTO (Response)
    public record EmployeeAttRuleResponse(
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
        public EmployeeAttRuleResponse(EmployeeAttRule rule,String employeeName) {
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

        public EmployeeAttRuleResponse(EmployeeAttRule rule) {
            this(rule, null); // 이름이 필요 없는 상황에는 null 전달
        }
    }

}
