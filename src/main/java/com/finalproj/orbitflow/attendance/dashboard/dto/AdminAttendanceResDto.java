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

    private Long attendanceId;      // 근태 기록 고유 ID
    private String employeeName;    // 사원 이름
    private String employeeNum;     // 사번 (예: 1001)
    private String workDate;        // 근무 일자 (YYYY-MM-DD)
    private String commuteAt;       // 출근 시각 (HH:mm:ss)
    private String leaveAt;         // 퇴근 시각 (HH:mm:ss)
    private String workingTime;     // 총 근무 시간
    private String statusName;      // 상태 명칭 (정상, 지각, 결근 등)
    private String statusCode;      // 상태 코드 (LATE, ABSENT 등)

    @JsonProperty("isCorrected")
    private boolean isCorrected;    // 수정 여부 (0 또는 1)
    private String correctionReason; // 정정 사유
}
