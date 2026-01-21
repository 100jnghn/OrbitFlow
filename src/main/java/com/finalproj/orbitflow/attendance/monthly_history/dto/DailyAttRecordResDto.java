package com.finalproj.orbitflow.attendance.monthly_history.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DailyAttRecordResDto
 * @since : 2025. 12. 21. 일요일
 */

@Getter
@Builder
public class DailyAttRecordResDto {
    private String date;
    private String commuteAt;
    private String leaveAt;
    private String workingTime;
    private String statusName;
    private String statusCode;
}