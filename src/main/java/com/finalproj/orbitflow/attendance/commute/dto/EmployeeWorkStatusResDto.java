package com.finalproj.orbitflow.attendance.commute.dto;

import com.finalproj.orbitflow.hr.employee.enums.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeWorkStatusResDto
 * @since : 2026-01-06 화요일
 */

@Getter
@AllArgsConstructor
public class EmployeeWorkStatusResDto {
    private WorkStatus workStatus;
}