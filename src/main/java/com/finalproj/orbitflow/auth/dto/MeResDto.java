package com.finalproj.orbitflow.auth.dto;

import com.finalproj.orbitflow.hr.employee.enums.Gender;
import com.finalproj.orbitflow.hr.employee.enums.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : MeResDto
 * @since : 2025-12-18 목요일
 */
@Getter
@AllArgsConstructor
public class MeResDto {
    private Long employeeId;
    private String name;
    private String email;
    private String role;
    private WorkStatus workStatus;
    private Gender gender;
    String companyName;
}
