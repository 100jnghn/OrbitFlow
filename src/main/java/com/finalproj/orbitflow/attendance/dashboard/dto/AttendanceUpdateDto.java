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

    private String status;           // 변경할 상태 (ON_TIME, LATE 등)
    private String commuteAt;        // 수정할 출근 시각 (선택 사항)
    private String leaveAt;          // 수정할 퇴근 시각 (선택 사항)
    private String correctionReason; // 수정 사유 (필수 입력 권장)
}
