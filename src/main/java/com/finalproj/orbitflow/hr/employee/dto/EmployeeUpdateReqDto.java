package com.finalproj.orbitflow.hr.employee.dto;

import com.finalproj.orbitflow.hr.employee.enums.EmployeeRole;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.enums.EmploymentType;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeUpdateReqDto
 * @since : 2025-12-29 월요일
 */
@Getter
public class EmployeeUpdateReqDto {

    private Long orgId;
    private Long rankId;
    private Long positionCategoryId;

    private EmploymentType employmentType;
    private EmployeeStatus status;
    private EmployeeRole role;
}
