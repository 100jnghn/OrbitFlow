package com.finalproj.orbitflow.attendance.leave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private Integer year;
    private BigDecimal totalGranted;
    private BigDecimal usedDays;
    private BigDecimal remainingDays;
    private LocalDate hireDate;
}
