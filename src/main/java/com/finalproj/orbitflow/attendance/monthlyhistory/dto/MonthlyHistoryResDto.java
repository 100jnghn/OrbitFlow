package com.finalproj.orbitflow.attendance.monthlyhistory.dto;

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
    private String searchPeriod;
    private MonthlyAttHistoryResDto summary;
    private Page<DailyAttRecordResDto> pagedData;
}
