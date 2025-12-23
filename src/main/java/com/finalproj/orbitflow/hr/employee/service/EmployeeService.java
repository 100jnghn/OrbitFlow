package com.finalproj.orbitflow.hr.employee.service;

import com.finalproj.orbitflow.hr.employee.dto.EmployeeResDto;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeService
 * @since : 2025-12-23 화요일
 */

@Service
@Transactional
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeeResDto> findByOrgAndPosition(
            Long companyId,
            Long orgId,
            Long positionCategoryId
    ) {
        return employeeRepository
                .findByCompany_IdAndOrganization_IdAndPositionCategory_IdAndStatus(
                        companyId,
                        orgId,
                        positionCategoryId,
                        EmployeeStatus.ACTIVE
                )
                .stream()
                .map(EmployeeResDto::from)
                .toList();
    }

}
