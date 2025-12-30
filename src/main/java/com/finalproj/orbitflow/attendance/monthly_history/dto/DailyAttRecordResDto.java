package com.finalproj.orbitflow.attendance.monthly_history.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DailyAttRecordResDto
 * @since : 2025. 12. 21. 일요일
 */
@Getter
@Builder
public class DailyAttRecordResDto {
    private String date;           // "12월 29일(월)"
    private String commuteAt;      // "08:58"
    private String leaveAt;        // "18:05" 또는 "미기록"
    private String workingTime;    // "8h 07m"
    private String statusName;     // "정상출근", "지각"
    private String statusCode;     // "LATE" 등 (CSS 클래스 바인딩용)
}