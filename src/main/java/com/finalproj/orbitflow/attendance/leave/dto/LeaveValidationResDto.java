package com.finalproj.orbitflow.attendance.leave.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : LeaveValidationResDto
 * @since : 26. 1. 6. 화요일
 **/

@Getter
@Builder
public class LeaveValidationResDto {
    Boolean valid;
    BigDecimal requiredDays;
    BigDecimal remainingDays;
    String message;
}
