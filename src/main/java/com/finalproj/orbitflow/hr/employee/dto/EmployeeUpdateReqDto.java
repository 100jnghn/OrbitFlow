package com.finalproj.orbitflow.hr.employee.dto;

import com.finalproj.orbitflow.hr.employee.enums.EmployeeRole;
import com.finalproj.orbitflow.hr.employee.enums.EmploymentType;
import com.finalproj.orbitflow.hr.employee.enums.Gender;
import jakarta.validation.constraints.Pattern;
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
       --> email 일부러 안 넣음
       (로그인 ID라 관리자 수정 불가 정책 유지)
       ============================== */

    private String name;
    private String employeeNo;
    @Pattern(regexp = "^[0-9]*$", message = "연락처는 숫자만 입력 가능합니다.")
    private String phone;

    @Pattern(regexp = "^[0-9]*$", message = "내선 번호는 숫자만 입력 가능합니다.")
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
