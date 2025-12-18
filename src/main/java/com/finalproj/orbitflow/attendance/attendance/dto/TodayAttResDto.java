package com.finalproj.orbitflow.attendance.attendance.dto;

import com.finalproj.orbitflow.attendance.attendance.entity.Attendance;
import com.finalproj.orbitflow.attendance.attendance.enums.AttendanceStatus;

import java.time.LocalDateTime;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : TodayAttResDto
 * @since : 2025. 12. 18. 목요일
 */

public record TodayAttResDto(
        Long attendanceId,
        LocalDateTime commuteAt,
        LocalDateTime leaveAt,
        AttendanceStatus status,
        String statusName,
        Boolean isAway
) {
    public TodayAttResDto(Attendance attendance, Boolean isAway) {
        this(
                attendance != null ? attendance.getId() : null,
                attendance != null ? attendance.getCommuteAt() : null,
                attendance != null ? attendance.getLeaveAt() : null,
                attendance != null ? attendance.getStatus() : null,
                // 상태가 없으면 "근무예정", 있으면 해당 설명 반환
                (attendance != null && attendance.getStatus() != null)
                        ? attendance.getStatus().getDescription()
                        : AttendanceStatus.BEFORE_WORK.getDescription(),
                isAway != null ? isAway : false
        );
    }
}
