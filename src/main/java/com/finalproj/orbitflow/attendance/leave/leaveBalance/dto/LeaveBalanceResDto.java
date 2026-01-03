package com.finalproj.orbitflow.attendance.leave.leaveBalance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : LeaveBalanceResDto
 * @since : 2025. 12. 27. 토요일
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceResDto {
    private Integer year;           // 기준 연도
    private BigDecimal totalGranted; // 총 발생 연차
    private BigDecimal usedDays;     // 사용 연차 (totalGranted - remainingDays 계산값)
    private BigDecimal remainingDays; // 잔여 연차
}
