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
    private String totalWorkTimeDisplay; // "160시간" 또는 "160h 30m" (UI 표시용)
    private long totalWorkMinutes;      // 내부 계산용 분 단위 합계

    private long lateCount;             // 지각 횟수
    private long leaveAbsentCount;      // 휴가/결근 일수
}