package com.finalproj.orbitflow.auth.controller;

import com.finalproj.orbitflow.auth.dto.LoginRequest;
import com.finalproj.orbitflow.auth.dto.LoginResponse;
import com.finalproj.orbitflow.global.security.CustomUserDetailsService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.global.security.jwt.JwtProvider;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuthController
 * @since : 2025-12-16 화요일
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        SecurityUser user = userDetailsService.loadByCompanyIdAndEmail(
                request.getCompanyId(),
                request.getEmail()
        );

        if (user.getStatus() != EmployeeStatus.ACTIVE) {
            throw new IllegalStateException("로그인할 수 없는 계정 상태입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        String token = jwtProvider.createToken(user);
        return new LoginResponse(token);
    }
}
