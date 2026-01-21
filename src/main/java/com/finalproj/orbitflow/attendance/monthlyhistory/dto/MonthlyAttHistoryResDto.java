package com.finalproj.orbitflow.attendance.monthlyhistory.dto;

import lombok.Builder;
import lombok.Getter;

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
    private String totalWorkTimeDisplay;
    private long totalWorkMinutes;

    private long lateCount;
    private long leaveAbsentCount;
}