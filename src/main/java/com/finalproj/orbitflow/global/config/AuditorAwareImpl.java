package com.finalproj.orbitflow.global.config;

import com.finalproj.orbitflow.global.security.SecurityUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Spring Data JPA Auditing에서 사용할 현재 사용자 식별자 제공 클래스.
 * Spring Security의 인증 정보를 기반으로 현재 로그인한 사원의 employeeId를 반환한다.
 * 이를 통해 @CreatedBy, @LastModifiedBy 필드가 자동으로 채워진다.
 *
 * @author : seunga03
 * @filename : AuditorAwareImpl
 * @since : 2025-12-15 월요일
 */
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        if (auth.getPrincipal() instanceof SecurityUser user) {
            return Optional.of(user.getEmployeeId());
        }

        return Optional.empty();
    }
}
