package com.finalproj.orbitflow.global.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : SecurityUtils
 * @since : 2025-12-17 수요일
 */
public class SecurityUtils {

    public static SecurityUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUser user)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        return user;
    }

    public static Long getCompanyId() {
        return getCurrentUser().getCompanyId();
    }

    public static Long getEmployeeId() {
        return getCurrentUser().getEmployeeId();
    }
}
