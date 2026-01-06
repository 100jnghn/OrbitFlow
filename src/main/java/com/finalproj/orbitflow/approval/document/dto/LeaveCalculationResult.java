package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;

import java.math.BigDecimal;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : LeaveCalculationResult
 * @since : 26. 1. 6. 화요일
 **/


public record LeaveCalculationResult(
        VacationPayload payload,
        LeaveType leaveType,
        BigDecimal days
) {
}
