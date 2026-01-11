package com.finalproj.orbitflow.global.security.jwt;

import com.finalproj.orbitflow.global.security.CustomUserDetailsService;
import com.finalproj.orbitflow.global.security.SecurityUser;
import com.finalproj.orbitflow.hr.employee.enums.EmployeeStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 요청에서 JWT 검사
 *
 * @author : seunga03
 * @filename : JwtAuthenticationFilter
 * @since : 2025-12-16 화요일
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return isPublicPath(path);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/refresh")
                || path.startsWith("/view/**")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/")
                || path.equals("/favicon.ico")
                || path.startsWith("/api/companies/")
                || path.startsWith("/companies/")

                // ✅ PDF 미리보기 (임시)
                || path.startsWith("/internal")
                || path.endsWith("/pdf");
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // 토큰은 있는데 유효하지 않으면 → 바로 401
            if (!jwtProvider.validateToken(token)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Long employeeId = jwtProvider.getEmployeeId(token);
            Long tokenCompanyId = jwtProvider.getCompanyId(token);

            SecurityUser user = userDetailsService.loadByEmployeeId(employeeId);

            // 테넌트 불일치
            if (!tokenCompanyId.equals(user.getCompanyId())) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 계정 상태 체크 (JWT 단계)
            if (user.getStatus() != EmployeeStatus.ACTIVE) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            var auth = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities()
            );
            auth.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
