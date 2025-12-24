package com.finalproj.orbitflow.auth.controller;

import com.finalproj.orbitflow.auth.dto.LoginReqDto;
import com.finalproj.orbitflow.auth.dto.LoginResDto;
import com.finalproj.orbitflow.auth.dto.MeResDto;
import com.finalproj.orbitflow.auth.entity.RefreshToken;
import com.finalproj.orbitflow.auth.service.AuthService;
import com.finalproj.orbitflow.global.common.ResponseDto;
import com.finalproj.orbitflow.global.exception.ForbiddenException;
import com.finalproj.orbitflow.global.exception.UnauthorizedException;
import com.finalproj.orbitflow.global.security.CustomUserDetailsService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.global.security.jwt.JwtProvider;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

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
    public ResponseEntity<ResponseDto> login(@RequestBody LoginReqDto request,
                                             HttpServletResponse response) {

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

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(false) // HTTPS면 true
                .path("/")
                .maxAge(Duration.between(Instant.now(), refreshToken.getExpiresAt()))
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        LoginResDto res = new LoginResDto(
                accessToken,
                null,
                refreshToken.getExpiresAt()
        );

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "로그인 성공", res));
    }

    /**
     * Access Token 재발급 (Refresh 만료 연장 X)
     */
    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto> refresh(
            @CookieValue(value = "refreshToken", required = false) String token
    ) {

        if (token == null) {
            throw new UnauthorizedException("리프레시 토큰이 없습니다.");
        }

        RefreshToken refreshToken = authService.validateRefreshToken(token);

        SecurityUser user =
                userDetailsService.loadByEmployeeId(refreshToken.getEmployeeId());

        if (user.getStatus() != EmployeeStatus.ACTIVE) {
            throw new ForbiddenException("로그인할 수 없는 계정 상태입니다.");
        }

        LoginResDto res = new LoginResDto(
                jwtProvider.createToken(user),
                null,
                refreshToken.getExpiresAt()
        );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "Access Token 재발급 성공", res)
        );
    }

    /**
     * 세션 연장 (사용자 명시적 선택 시)
     * - Refresh Token Rotation
     * - 만료 20시간 재설정
     */
    /**
     * 세션 연장 (사용자 명시적 선택 시)
     * - Refresh Token Rotation
     * - 만료 20시간 재설정
     */
    @PostMapping("/extend-session")
    public ResponseEntity<ResponseDto> extendSession(
            @CookieValue(value = "refreshToken", required = false) String token,
            HttpServletResponse response
    ) {
        if (token == null) {
            throw new UnauthorizedException("리프레시 토큰이 없습니다.");
        }

        // 기존 Refresh Token 검증
        RefreshToken oldToken = authService.validateRefreshToken(token);

        SecurityUser user =
                userDetailsService.loadByEmployeeId(oldToken.getEmployeeId());

        if (user.getStatus() != EmployeeStatus.ACTIVE) {
            throw new ForbiddenException("로그인할 수 없는 계정 상태입니다.");
        }

        // 기존 Refresh Token 폐기 (Rotation)
        authService.invalidateRefreshToken(token);

        // 새 Refresh Token 발급 (20시간)
        RefreshToken newRefreshToken = authService.issueRefreshToken(user);

        // 새 Refresh Token을 HttpOnly Cookie로 재설정
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken.getToken())
                .httpOnly(true)
                .secure(false) // HTTPS 환경이면 true
                .path("/")
                .maxAge(Duration.between(Instant.now(), newRefreshToken.getExpiresAt()))
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // 새 Access Token 발급
        LoginResDto res = new LoginResDto(
                jwtProvider.createToken(user),
                null,
                newRefreshToken.getExpiresAt()
        );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "세션 연장 완료", res)
        );
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ResponseDto> logout(
            @CookieValue(value = "refreshToken", required = false) String token,
            HttpServletResponse response
    ) {
        if (token != null) {
            authService.invalidateRefreshToken(token);
        }

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(new ResponseDto(HttpStatus.OK, "로그아웃 완료", null));
    }

    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ResponseDto> me(
            @AuthenticationPrincipal SecurityUser user
    ) {
        if (user == null) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        MeResDto res = new MeResDto(
                user.getEmployeeId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(
                new ResponseDto(HttpStatus.OK, "내 정보 조회 성공", res)
        );
    }
}
