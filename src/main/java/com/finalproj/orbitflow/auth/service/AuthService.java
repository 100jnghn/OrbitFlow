package com.finalproj.orbitflow.auth.service;

import com.finalproj.orbitflow.auth.entity.RefreshToken;
import com.finalproj.orbitflow.auth.repository.RefreshTokenRepository;
import com.finalproj.orbitflow.global.exception.UnauthorizedException;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AuthService
 * @since : 2025-12-18 목요일
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    //private static final long REFRESH_TOKEN_TTL_SECONDS = 1860; // 2분 (테스트용)
    private static final long REFRESH_TOKEN_TTL_SECONDS = 60 * 60 * 20;


    // Refresh Token 생성 + 저장
    public RefreshToken issueRefreshToken(SecurityUser user) {
        Instant expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS);

        return refreshTokenRepository.save(
                new RefreshToken(
                        user.getCompanyId(),
                        user.getEmployeeId(),
                        UUID.randomUUID().toString(),
                        expiresAt
                )
        );
    }

    // Refresh Token 검증
    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("유효하지 않은 토큰입니다."));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("만료된 토큰입니다.");
        }

        return refreshToken;
    }

    // 로그아웃
    public void invalidateRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    // 비밀번호 변경 시 Refresh Token 전체 무효화
    public void invalidateAll(Long employeeId) { // 특정 사원의 모든 Refresh Token을 DB에서 삭제
        refreshTokenRepository.deleteAllByEmployeeId(employeeId);
    }
}
