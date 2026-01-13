package com.finalproj.orbitflow.attendance.rule.dto.request;

import java.time.LocalTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceRuleUpdateReqDto
 * @since : 2025. 12. 18. 목요일
 */

public record DefaultRuleUpdateReqDto(
                LocalTime defaultStartTime, // 기본 출근시간
                LocalTime defaultEndTime, // 기본 퇴근시간

                Integer defaultBreakMinutes // 기본 휴게시간

) {
}
