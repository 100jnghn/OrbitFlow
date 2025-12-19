package com.finalproj.orbitflow.attendance.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DashBoardSummaryDto
 * @since : 2025. 12. 19. 금요일
 */

@Getter
@Builder
public class DashBoardSummaryDto {

    private long totalEmployeeCount;    // 전체 직원 수
    private long checkedInCount;       // 출근 완료 인원
    private long lateCount;            // 금일 지각 인원
    private long workingCount;         // 퇴근 미처리(근무 중) 인원
    private long correctionRequestCount; // 정정 요청 대기 건수
}
