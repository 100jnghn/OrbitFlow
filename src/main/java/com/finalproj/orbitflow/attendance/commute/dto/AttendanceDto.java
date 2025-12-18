package com.finalproj.orbitflow.attendance.commute.dto;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;
import com.finalproj.orbitflow.attendance.commute.enums.AttendanceStatus;
import java.time.LocalDateTime;

public class AttendanceDto {

    public record TodayAttendanceResponse(
            Long attendanceId,
            LocalDateTime commuteAt,
            LocalDateTime leaveAt,
            AttendanceStatus status,
            String statusName,
            Boolean isAway
    ) {
        public TodayAttendanceResponse(Attendance attendance, Boolean isAway) {
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
}