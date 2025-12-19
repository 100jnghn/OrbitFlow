package com.finalproj.orbitflow.attendance.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : SearchConditionDto
 * @since : 2025. 12. 19. 금요일
 */

@Getter
@Setter
public class SearchConditionDto {
    private String startDate;          // 조회 시작일
    private String endDate;            // 조회 종료일
    private String status;             // 근무 상태 필터 (지각, 조퇴 등)
}
