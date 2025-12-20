package com.finalproj.orbitflow.auth.controller;

import com.finalproj.orbitflow.auth.dto.LoginReqDto;
import com.finalproj.orbitflow.auth.dto.LoginResDto;
import com.finalproj.orbitflow.auth.dto.MeResDto;
import com.finalproj.orbitflow.auth.entity.RefreshToken;
import com.finalproj.orbitflow.auth.service.AuthService;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.UnauthorizedException;
import com.finalproj.orbitflow.global.security.CustomUserDetailsService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.global.security.jwt.JwtProvider;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    private final AuthService authService;

    /**
     * 로그인
     */
    @PostMapping("/login")
    public LoginResDto login(@RequestBody LoginReqDto request) {

        SecurityUser user;
        try {
            user = userDetailsService.loadByEmail(request.getEmail());
        } catch (UsernameNotFoundException e) { // email 없는 경우 처리
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (user.getStatus() != EmployeeStatus.ACTIVE) {
            throw new ForbiddenException("로그인할 수 없는 계정 상태입니다.");
        }

        // 동시 로그인 차단하려면 주석 풀기
//        authService.invalidateAll(user.getEmployeeId());


        String accessToken = jwtProvider.createToken(user);
        RefreshToken refreshToken = authService.issueRefreshToken(user);

        return new LoginResDto(
                accessToken,
                refreshToken.getToken(),
                refreshToken.getExpiresAt()
        );
    }

    /**
     * Access Token 재발급 (Refresh 만료 연장 X)
     */
    @PostMapping("/refresh")
    public LoginResDto refresh(@RequestHeader("Refresh-Token") String token) {

        RefreshToken refreshToken = authService.validateRefreshToken(token);

        SecurityUser user =
                userDetailsService.loadByEmployeeId(refreshToken.getEmployeeId());

        if (user.getStatus() != EmployeeStatus.ACTIVE) {
            throw new ForbiddenException("로그인할 수 없는 계정 상태입니다.");
        }

        String newAccessToken = jwtProvider.createToken(user);

        return new LoginResDto(
                newAccessToken,
                null,
                refreshToken.getExpiresAt()
        );
    }

    /**
     * 세션 연장 (사용자 명시적 선택 시)
     * - Refresh Token Rotation
     * - 만료 20시간 재설정
     */
    @PostMapping("/extend-session")
    public LoginResDto extendSession(@RequestHeader("Refresh-Token") String token) {

        RefreshToken oldToken = authService.validateRefreshToken(token);

        SecurityUser user =
                userDetailsService.loadByEmployeeId(oldToken.getEmployeeId());

        if (user.getStatus() != EmployeeStatus.ACTIVE) {
            throw new ForbiddenException("로그인할 수 없는 계정 상태입니다.");
        }

        // 기존 Refresh Token 폐기
        authService.invalidateRefreshToken(token);

        // 새 Refresh Token 발급 (20시간)
        RefreshToken newRefreshToken = authService.issueRefreshToken(user);

        String newAccessToken = jwtProvider.createToken(user);

        return new LoginResDto(
                newAccessToken,
                newRefreshToken.getToken(),
                newRefreshToken.getExpiresAt()
        );
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "Refresh-Token", required = false) String token) {
        if (token != null) {
            authService.invalidateRefreshToken(token);
        }
    }

    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    public MeResDto me(@AuthenticationPrincipal SecurityUser user) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        return new MeResDto(
                user.getEmployeeId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
