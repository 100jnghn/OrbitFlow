package com.finalproj.orbitflow.global.security;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeRole;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security 인증 객체로 사용되는 사용자 정보 클래스.
 * Employee 엔티티를 기반으로 인증에 필요한 정보만 분리하여 보관하며,
 * Auditing 연동을 위해 employeeId를 포함한다.
 *
 * @author : seunga03
 * @filename : SecurityUser
 * @since : 2025-12-15 월요일
 */
@Getter
public class SecurityUser implements UserDetails {

    private final Long employeeId;
    private final Long companyId;
    private final String email;
    private final String password;
    private final EmployeeStatus status;
    private final EmployeeRole role;

    public SecurityUser(Employee employee) {
        this.employeeId = employee.getId();
        this.companyId = employee.getCompany().getId();
        this.email = employee.getEmail();
        this.password = employee.getPassword();
        this.status = employee.getStatus();
        this.role = employee.getRole();
    }

    // 로그인 ID
    @Override
    public String getUsername() {
        return email;
    }

    // 권한
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 정지 계정 잠금
        return status != EmployeeStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 퇴사 계정 비활성
        return status == EmployeeStatus.ACTIVE;
    }
}