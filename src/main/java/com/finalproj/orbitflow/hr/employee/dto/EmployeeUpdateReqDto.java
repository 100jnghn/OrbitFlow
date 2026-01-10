package com.finalproj.orbitflow.hr.employee.dto;

import com.finalproj.orbitflow.hr.employee.enums.EmployeeRole;
import com.finalproj.orbitflow.hr.employee.enums.EmploymentType;
import com.finalproj.orbitflow.hr.employee.enums.Gender;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeUpdateReqDto
 * @since : 2025-12-29 월요일
 */
@Getter
public class EmployeeUpdateReqDto {

    /* ==============================
       기본 정보 (관리자 수정 가능)
       --> email, employeeNo 일부러 안 넣음
       (로그인 ID + 인사 기준값이라 관리자 수정 불가 정책 유지)
       ============================== */

    private String name;
    private String phone;
    private String internalPhone;
    private LocalDate birthDate;
    private LocalDate hireDate;
    private Gender gender;

    private Long orgId;
    private Long rankId;
    private Long positionCategoryId;

    private EmploymentType employmentType;
//    private EmployeeStatus status;
    private EmployeeRole role;
}
