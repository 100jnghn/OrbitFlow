package com.finalproj.orbitflow.approval.document.support.approver;

import com.finalproj.orbitflow.approval.approvalLine.entity.ApprovalLine;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : ApproverResolver
 * @since : 26. 1. 21. 수요일
 **/


@Component
@RequiredArgsConstructor
public class ApproverResolver {

    private final EmployeeRepository employeeRepository;

    public boolean isValid(Employee employee) {
        return employee != null && employee.getStatus() == EmployeeStatus.ACTIVE;
    }

    public boolean matchesRole(ApprovalLine line, Employee employee) {
        return employee != null
                && Objects.equals(employee.getOrganization().getId(), line.getOrganization().getId())
                && Objects.equals(employee.getPositionCategory().getId(), line.getPositionCategory().getId());
    }

    public Employee findReplacement(ApprovalLine line) {
        if (line.getOrganization() == null || line.getPositionCategory() == null) {
            return null;
        }

        return employeeRepository
                .findHeadByOrgIdAndPositionCategoryIdAndStatus(
                        line.getOrganization().getId(),
                        line.getPositionCategory().getId(),
                        EmployeeStatus.ACTIVE
                )
                .orElse(null);
    }
}
