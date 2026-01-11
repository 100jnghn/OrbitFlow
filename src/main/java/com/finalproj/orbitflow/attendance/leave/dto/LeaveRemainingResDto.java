package com.finalproj.orbitflow.attendance.leave.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : LeaveRemainingResDto
 * @since : 26. 1. 6. 화요일
 **/

@Getter
@AllArgsConstructor
public class LeaveRemainingResDto {
    BigDecimal leaveRemaining;
}
