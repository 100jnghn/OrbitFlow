package com.finalproj.orbitflow.attendance.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AdminSummaryResDto
 * @since : 2025. 12. 22. 월요일
 */

@Getter
@Builder
public class AdminSummaryResDto {
    private int totalEmployees;
    private int onTimeCount;
    private int lateCount;
    private int absentCount;
    private int vacationCount;
    private int outsideCount;
    private int businessTripCount;
}