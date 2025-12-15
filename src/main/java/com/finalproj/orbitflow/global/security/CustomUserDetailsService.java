package com.finalproj.orbitflow.global.security;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : CustomUserDetailsService
 * @since : 2025-12-15 월요일
 */

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String employeeNo)
            throws UsernameNotFoundException {

        Employee employee = employeeRepository
                .findByEmail(employeeNo)
                .orElseThrow(() ->
                        new UsernameNotFoundException("사원 정보 없음"));

        return new SecurityUser(employee);
    }
}