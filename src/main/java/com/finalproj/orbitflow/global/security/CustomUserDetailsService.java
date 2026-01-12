package com.finalproj.orbitflow.global.security;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security에서 사용하는 사용자 인증 로직 구현체.
 * 로그인 요청 시 전달된 ID(email)를 기반으로 사원 정보를 조회하여 SecurityUser로 변환한다.
 *
 * @author : seunga03
 * @filename : CustomUserDetailsService
 * @since : 2025-12-15 월요일
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService {

    private final EmployeeRepository employeeRepository;

    /**
     * 로그인 시 사용 (email 기준)
     */
    public SecurityUser loadByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사원 정보 없음"));
        return new SecurityUser(employee);
    }

    /**
     * JWT 인증 시 사용 (employee 테이블의 id 기준)
     */
    public SecurityUser loadByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new UsernameNotFoundException("사원 정보 없음"));
        return new SecurityUser(employee);
    }

}
