package com.finalproj.orbitflow.global.security;

import com.finalproj.orbitflow.hr.employee.entity.Employee;
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
    private final String employeeNo;
    private final String password;

    public SecurityUser(Employee employee) {
        this.employeeId = employee.getEmployeeId();
        this.employeeNo = employee.getEmail();
        this.password = employee.getPassword();
    }

    // 권한
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(new SimpleGrantedAuthority(role));
//    }

    // 로그인 ID
    @Override
    public String getUsername() {
        return employeeNo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    // 계정 상태 제어
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

//    @Override
//    public boolean isAccountNonLocked() {
//        return !"SUSPENDED".equals(status);
//    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

//    @Override
//    public boolean isEnabled() {
//        return "ACTIVE".equals(status);
//    }
}