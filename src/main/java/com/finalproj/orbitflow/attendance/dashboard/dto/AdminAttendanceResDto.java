package com.finalproj.orbitflow.attendance.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AdminAttendanceResDto
 * @since : 2025. 12. 22. 월요일
 */

@Getter
@Setter
@Builder
public class AdminAttendanceResDto {

    private Long attendanceId;
    private String employeeName;
    private String employeeNum;
    private String workDate;
    private String commuteAt;
    private String leaveAt;
    private String workingTime;
    private String statusName;
    private String statusCode;

    @JsonProperty("isCorrected")
    private boolean isCorrected;
    private String correctionReason;
}
