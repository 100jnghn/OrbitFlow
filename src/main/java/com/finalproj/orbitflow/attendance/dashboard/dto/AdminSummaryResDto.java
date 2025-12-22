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

    private int totalEmployees;     // 전체 직원 수
    private int onTimeCount;        // 출근 완료 인원
    private int lateCount;          // 금일 지각 인원
    private int notLeavingCount;    // 퇴근 미처리 인원
    private int pendingRequestCount; // 정정 요청 대기 건수
}
