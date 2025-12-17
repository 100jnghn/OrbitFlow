package com.finalproj.orbitflow.auth.controller;

import com.finalproj.orbitflow.auth.dto.LoginReqDto;
import com.finalproj.orbitflow.auth.dto.LoginResDto;
import com.finalproj.orbitflow.global.security.CustomUserDetailsService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.global.security.jwt.JwtProvider;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
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
    // Spring Security AuthenticationManager를 사용하지 않고
    // 회사 ID + 이메일 기반의 커스텀 로그인 정책을 적용한다.
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public LoginResDto login(@RequestBody LoginReqDto request) {

        SecurityUser user = userDetailsService.loadByEmail(request.getEmail());


        if (user.getStatus() != EmployeeStatus.ACTIVE) {
            throw new AccessDeniedException("로그인할 수 없는 계정 상태입니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
        }

        String token = jwtProvider.createToken(user);
        return new LoginResDto(token);
    }
}
