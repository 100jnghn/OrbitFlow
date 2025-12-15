package com.finalproj.orbitflow.global.config;

import com.finalproj.orbitflow.global.security.SecurityUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Please explain the class!!!
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
