package com.finalproj.orbitflow.attendance.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : AttendanceListDto
 * @since : 2025. 12. 19. 금요일
 */

@Getter
@Builder
public class DashBoardListDto {

    private Long employeeId;           // 사원 ID
    private String employeeNo;         // 사번 (OF-001 등)
    private String name;               // 이름
    private String departmentName;     // 부서명 (Organization 테이블 조인)
    private String checkInTime;        // 출근 시각 (HH:mm:ss)
    private String checkOutTime;       // 퇴근 시각 (HH:mm:ss)
    private String totalWorkTime;      // 총 근무 시간 (계산된 값: 8시간 10분 등)
    private String status;             // 상태 (정상, 지각, 유연 근무, 연차 등)
    private boolean hasCorrectionRequest; // 정정 버튼 노출 여부
}
