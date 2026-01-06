package com.finalproj.orbitflow.attendance.leave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : LeaveValidationReqDto
 * @since : 26. 1. 6. 화요일
 **/

@Getter
@AllArgsConstructor
@Builder
public class LeaveValidationReqDto {
    LocalDate startDate;
    LocalDate endDate;
    Long leaveTypeId;
}
