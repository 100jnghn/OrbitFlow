package com.finalproj.orbitflow.leave.leaveBalance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveHistoryResDto
 * @since : 2025. 12. 27. 토요일
 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveHistoryResDto {
    private String title;      // 항목명 (예: "정기 연차 부여", "연차 휴가 사용") - LeaveType.typeName
    private String actionDate; // 발생/사용일 (yyyy-MM-dd)
    private String period;     // 기간 (사용 시 "2025-07-01 ~ 2025-07-02" 등 표시) - AttendanceRecord 기간
    private BigDecimal days;   // 일수 - AttendanceRecord.days
    private String type;       // 구분 ("GRANT": 부여, "USED": 사용)
    private String statusName; // 상태 (예: "완료", "승인대기", "소멸") - AttendanceRecord.status
    private String statusCode; // 상태 코드 (CSS 클래스 적용용: "COMPLETED", "WAITING", "EXPIRED")
    private String reason;     // 사유 - AttendanceRecord.reason
    private String typeDescription; // 휴가 유형 설명 - LeaveType.description
}