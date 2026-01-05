package com.finalproj.orbitflow.hr.employee.dto;

import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeStatusUpdateReqDto
 * @since : 2025-12-31 수요일
 */
@Getter
public class EmployeeStatusUpdateReqDto {
    @NotNull
    private EmployeeStatus status;
}
