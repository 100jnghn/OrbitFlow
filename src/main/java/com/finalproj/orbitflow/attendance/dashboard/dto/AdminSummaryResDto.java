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
    private int totalEmployees;     // 전체 사원 수
    private int onTimeCount;        // 출근 완료 (정상 + 지각)
    private int lateCount;          // 지각 인원
    private int absentCount;        // 순수 결근/미출근 (사유 없음)
    private int vacationCount;      // 휴가 중
    private int outsideCount;       // 외근 중
    private int businessTripCount;  // 출장 중
}