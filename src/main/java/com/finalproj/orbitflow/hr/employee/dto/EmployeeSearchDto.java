package com.finalproj.orbitflow.hr.employee.dto;

import com.finalproj.orbitflow.hr.employee.entity.Employee;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeSearchDto
 * @since : 2025-12-30 화요일
 */
public record EmployeeSearchDto(
        Long id,
        String name,
        String employeeNo,
        String email,
        String organizationName,
        String positionName
) {

    public static EmployeeSearchDto from(Employee emp) {
        return new EmployeeSearchDto(
                emp.getId(),
                emp.getName(),
                emp.getEmployeeNo(),
                emp.getEmail() != null ? emp.getEmail() : "",
                emp.getOrganization() != null ? emp.getOrganization().getName() : "",
                emp.getPositionCategory() != null ? emp.getPositionCategory().getName() : ""
        );
    }
}
