package com.finalproj.orbitflow.global.security.jwt;

import com.finalproj.orbitflow.auth.repository.RefreshTokenRepository;
import com.finalproj.orbitflow.global.security.CustomUserDetailsService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * 모든 요청에서 JWT 검사
 *
 * @author : seunga03
 * @filename : JwtAuthenticationFilter
 * @since : 2025-12-16 화요일
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();
        log.info("[JWT FILTER] shouldNotFilter check: {}", path);

        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            log.info("[JWT FILTER] skip because ASYNC");
            return true;
        }

        boolean isPublic = isPublicPath(path);
        log.info("[JWT FILTER] isPublicPath = {}", isPublic);

        return isPublic;
    }


    private boolean isPublicPath(String path) {
        // SSR 페이지(/view/**, /)는 이제 필터를 거쳐야 인증 정보(companyName 등)를 얻을 수 있음.
        // 따라서 정적 자원이나 인증이 필요 없는 API 엔드포인트만 제외
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/refresh")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/api/companies/")
                || path.startsWith("/companies/")
                || path.startsWith("/internal")
                || path.endsWith("/pdf");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        log.info("[JWT FILTER] {} {}", request.getMethod(), request.getRequestURI());


        String header = request.getHeader("Authorization");
        log.info("[JWT FILTER] Authorization header = {}", header);


        // 1. Authorization 헤더 확인 (API 요청)
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (!jwtProvider.validateToken(token)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Long employeeId = jwtProvider.getEmployeeId(token);
            Long tokenCompanyId = jwtProvider.getCompanyId(token);

            authenticateUser(request, response, employeeId, tokenCompanyId);
        }

        // refresh token으로 인증하는 로직 제거 (인증 = access token only)

        filterChain.doFilter(request, response);
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;
        return Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void authenticateUser(HttpServletRequest request, HttpServletResponse response, Long employeeId, Long companyId) {
        log.info("[JWT FILTER] authenticateUser employeeId={}, companyId={}",
                employeeId, companyId);
        SecurityUser user = userDetailsService.loadByEmployeeId(employeeId);

        // 테넌트 불일치
        if (!companyId.equals(user.getCompanyId())) {
            throw new RuntimeException("Tenant mismatch");
        }

        // 계정 상태 체크
        if (user.getStatus() != EmployeeStatus.ACTIVE) {
            throw new RuntimeException("Inactive user");
        }

        var auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        auth.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
