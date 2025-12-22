package com.finalproj.orbitflow.attendance.history.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : MonthlyHistoryResDto
 * @since : 2025. 12. 22. 월요일
 */

@Getter
@Builder
public class MonthlyHistoryResDto {
    private MonthlyAttHistoryResDto summary;        // 상단 통계
    private Page<DailyAttRecordResDto> pagedData;
}
