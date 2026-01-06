package com.finalproj.orbitflow.hr.organization.dto.sidebar;

import com.finalproj.orbitflow.hr.employee.enums.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgSidebarEmployeeDto
 * @since : 2026-01-05 월요일
 */
@Getter
@AllArgsConstructor
public class OrgSidebarEmployeeDto {

    private Long employeeId;
    private String name;
    private String internalPhone;
    private WorkStatus workStatus;

}
