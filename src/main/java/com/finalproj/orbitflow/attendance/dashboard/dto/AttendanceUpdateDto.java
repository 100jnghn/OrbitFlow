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
    private Long employeeId;
    private String status;
    private String correctionReason;
    private String commuteAt;
    private String leaveAt;
}
