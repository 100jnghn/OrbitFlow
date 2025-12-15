package com.finalproj.orbitflow.hr.employee.repository;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeRepository
 * @since : 2025-12-15 월요일
 */

public interface EmployeeRepository extends JpaRepository<Employee, Long> {


    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeNo(String employeeNo);

}
