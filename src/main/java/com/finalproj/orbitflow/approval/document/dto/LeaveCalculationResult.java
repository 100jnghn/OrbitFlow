package com.finalproj.orbitflow.approval.document.dto;

import com.finalproj.orbitflow.attendance.leave.entity.LeaveType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : LeaveCalculationResult
 * @since : 26. 1. 6. 화요일
 **/


public record LeaveCalculationResult(
        VacationPayload payload,        // 요청 원본
        LeaveType leaveType,
        BigDecimal days,                // 실제 차감 일수
        List<LocalDate> effectiveDates  // 실제 휴가 날짜
) {
}
