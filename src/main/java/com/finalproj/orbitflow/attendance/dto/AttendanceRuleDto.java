package com.finalproj.orbitflow.attendance.dto;

import com.finalproj.orbitflow.attendance.entity.AttendanceRule;

import java.time.LocalTime;

public class AttendanceRuleDto {

    // 기본 규칙 응답 DTO (Response)
    public record AttendanceRuleResponse(
            LocalTime defaultStartTime,
            LocalTime defaultEndTime,
            Integer defaultBreakMinutes
    ) {
        public AttendanceRuleResponse(AttendanceRule rule) {
            this(
                    rule.getDefaultStartTime(),
                    rule.getDefaultEndTime(),
                    rule.getDefaultBreakMinutes()
            );
        }
    }

    // 기본 규칙 수정 요청 DTO (PUT /default)
    public record AttendanceRuleUpdateRequest(
            LocalTime defaultStartTime,
            LocalTime defaultEndTime,
            Integer defaultBreakMinutes
    ) {}


}
