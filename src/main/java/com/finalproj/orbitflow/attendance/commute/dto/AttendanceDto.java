package com.finalproj.orbitflow.attendance.commute.dto;

import com.finalproj.orbitflow.attendance.commute.entity.Attendance;

import java.time.LocalDateTime;

public class AttendanceDto {

    // 오늘의 출퇴근 기록 응답 DTO
    public record TodayAttendanceResponse(
            Long attendanceId,
            LocalDateTime commuteAt,
            LocalDateTime leaveAt,
            String status
    ) {
        public TodayAttendanceResponse(Attendance attendance) {
            this(
                    attendance != null ? attendance.getId() : null,
                    attendance != null ? attendance.getCommuteAt() : null,
                    attendance != null ? attendance.getLeaveAt() : null,
                    attendance != null ? attendance.getStatus() : null
            );
        }
        
        // 빈 응답을 위한 생성자
        public TodayAttendanceResponse() {
            this(null, null, null, null);
        }
    }

    // 출근 요청 DTO
    public record CheckInRequest(
            Long employeeId,
            Long companyId
    ) {}

    // 퇴근 요청 DTO
    public record CheckOutRequest(
            Long employeeId
    ) {}
}

