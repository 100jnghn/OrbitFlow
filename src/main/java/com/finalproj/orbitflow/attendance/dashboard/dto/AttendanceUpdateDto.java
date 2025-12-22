package com.finalproj.orbitflow.attendance.dashboard.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceUpdateDto
 * @since : 2025. 12. 22. 월요일
 */
@Getter
@Setter
public class AttendanceUpdateDto {
    private Long employeeId;      // 기록 누락자 신규 생성 시 필수
    private String status;        // ON_TIME, LATE, ABSENT 등
    private String correctionReason;
    private String commuteAt;     // "09:00:00" 형식 (선택)
    private String leaveAt;       // "18:00:00" 형식 (선택)
}
