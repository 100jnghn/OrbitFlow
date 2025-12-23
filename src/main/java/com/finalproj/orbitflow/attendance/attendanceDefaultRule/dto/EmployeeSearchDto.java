package com.finalproj.orbitflow.attendance.attendanceDefaultRule.dto;

import com.finalproj.orbitflow.hr.employee.entity.Employee;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : EmployeeSearchDto
 * @since : 2025. 12. 22. 월요일
 */

public record EmployeeSearchDto(
        Long id,
        String name,
        String employeeNo,
        String organizationName,
        String positionName
) {

    public static EmployeeSearchDto from(Employee emp) {
        return new EmployeeSearchDto(
                emp.getId(),
                emp.getName(),
                emp.getEmployeeNo(),
                emp.getOrganization() != null ? emp.getOrganization().getName() : "",
                emp.getPositionCategory() != null ? emp.getPositionCategory().getName() : ""
        );
    }
}
