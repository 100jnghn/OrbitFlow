package com.finalproj.orbitflow.global.security;

import com.finalproj.orbitflow.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 전반 설정을 담당하는 클래스.
 * - 인증이 필요한 요청과 허용 요청을 구분한다.
 * - 커스텀 UserDetailsService를 통해 사원 기반 로그인을 처리한다.
 * - Form Login 방식을 사용하며, 추후 JWT 등으로 확장 가능하다.
 *
 * @author : seunga03
 * @filename : SecurityConfig
 * @since : 2025-12-15 월요일
 */

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/api/auth/**",
                                "/login",
                                "/view/**",
                                "/companies/**",
                                "/api/companies/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/internal/**",
                                "/favicon.ico",
                                "/activate/**",
                                "/reset-password/**",
                                "/find-password/**",
                                "/api/email/**",
                                "/actuator/health" //배포용 링크 permitAll 필수
                        ).permitAll()

                        .requestMatchers("/api/chatbot/**").authenticated()
                        .requestMatchers("/api/manual/**").authenticated()

                        .requestMatchers("/api/admin/**")
                        .hasAnyRole("ADMIN", "COMPANY_ADMIN")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}