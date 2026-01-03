package com.finalproj.orbitflow.attendance.leave.leaveHistory.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveHistoryResDto
 * @since : 2025. 12. 29. 월요일
 */

@Getter
@Builder
public class LeaveHistoryResDto {
    private String title;       // 휴가 종류 (LeaveType.typeName)
    private String period;      // 휴가 기간 (startDate ~ endDate)
    private BigDecimal days;    // 사용 일수/시간
    private String actionDate;  // 신청일 (createdAt)
    private String statusName;  // 결제 상태 명칭 (승인, 대기, 반려)
    private String statusCode;  // 결제 상태 코드 (APPROVED, SUBMITTED, REJECTED)
    private String type;        // 구분 (GRANT: 부여, USED: 사용)
}