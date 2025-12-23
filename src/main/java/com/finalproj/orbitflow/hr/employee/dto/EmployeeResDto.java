package com.finalproj.orbitflow.hr.employee.dto;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeResDto
 * @since : 2025-12-23 화요일
 */
@Getter
@AllArgsConstructor
public class EmployeeResDto {

    // 현재는 누군지 식별 + 화면 표시용 수준으로 구현해놓음

    private Long id;
    private String name;
    private String employeeNo;
    private String email;

    public static EmployeeResDto from(Employee e) {
        return new EmployeeResDto(
                e.getId(),
                e.getName(),
                e.getEmployeeNo(),
                e.getEmail()
        );
    }
}

