package com.finalproj.orbitflow.attendance.dashboard.dto;

import lombok.Builder;
import org.springframework.data.domain.Page;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceSummaryResDto
 * @since : 2025. 12. 19. 금요일
 */

@Builder
public class AttendanceDashboardResDto {

    // 1. 금일 요약 현황 (이미지 2번 영역)
    private DashBoardSummaryDto summary;

    // 2. 직원별 근태 상세 목록 (이미지 4번 영역)
    private Page<DashBoardListDto> attendanceList;
}
