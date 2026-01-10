package com.finalproj.orbitflow.hr.employee.dto;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeRole;
import com.finalproj.orbitflow.hr.employee.enums.EmploymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeEditResDto
 * @since : 2026-01-02 금요일
 */
@Getter
@AllArgsConstructor
public class EmployeeUpdateResDto {

    private String name;
    private String phone;
    private String internalPhone;
    private LocalDate birthDate;

    private LocalDate hireDate;

    private Long orgId;
    private Long rankId;
    private Long positionCategoryId;

    private EmploymentType employmentType;
    private EmployeeRole role;

    // 화면 표시용(선택) : 모달 상단에 “email/사번” 보여주고 싶으면
    private String email;
    private String employeeNo;

    public static EmployeeUpdateResDto from(Employee e) {
        return new EmployeeUpdateResDto(
                e.getName(),
                e.getPhone(),
                e.getInternalPhone(),
                e.getBirthDate(),
                e.getHireDate(),
                e.getOrganization().getId(),
                e.getRank() != null ? e.getRank().getId() : null,
                e.getPositionCategory() != null ? e.getPositionCategory().getId() : null,
                e.getEmploymentType(),
                e.getRole(),
                e.getEmail(),
                e.getEmployeeNo()
        );
    }
}
