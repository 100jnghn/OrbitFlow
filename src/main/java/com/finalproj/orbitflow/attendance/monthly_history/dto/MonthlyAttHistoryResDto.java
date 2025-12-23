package com.finalproj.orbitflow.attendance.monthly_history.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : MonthlyAttHistoryResDto
 * @since : 2025. 12. 21. 일요일
 */
@Getter
@Builder
public class MonthlyAttHistoryResDto {
    private long totalWorkHours;    // 총 근무 시간
    private long lateCount;         // 지각 횟수
    private long leaveAbsentCount;  // 휴가/결근 일수
    private List<DailyAttRecordResDto> dailyRecords;
}