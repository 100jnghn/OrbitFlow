package com.finalproj.orbitflow.hr.employee.dto;

import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.enums.EmploymentType;
import com.finalproj.orbitflow.hr.employee.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeDetailResDto
 * @since : 2025-12-29 월요일
 */
@Getter
@AllArgsConstructor
public class EmployeeDetailResDto {

    private Long id;
    private String name;
    private String email;
    private String employeeNo;

    private Gender gender;
    private LocalDate birthDate;

    private String phone;
    private String internalPhone;

    private LocalDate hireDate;
    private EmploymentType employmentType;
    private EmployeeStatus status;

    private String orgPath;
    private String rankName;
    private String positionName;
}
