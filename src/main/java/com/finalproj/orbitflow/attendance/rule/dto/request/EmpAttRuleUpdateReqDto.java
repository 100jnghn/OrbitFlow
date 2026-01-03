package com.finalproj.orbitflow.attendance.rule.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : EmpAttRuleUpdateReqDto
 * @since : 2025. 12. 18. 목요일
 */

// 규칙 수정 요청 DTO (PUT /exception/{ruleId})
public record EmpAttRuleUpdateReqDto(
        LocalTime startTime,
        LocalTime endTime,
        Integer breakMinutes,
        String reason,
        LocalDate validFrom,
        LocalDate validTo,
        Boolean isActive
) {
}
