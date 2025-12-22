package com.finalproj.orbitflow.global.security;

import com.finalproj.orbitflow.global.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : SecurityUtils
 * @since : 2025-12-17 수요일
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    private static SecurityUser getRequiredUser() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof SecurityUser user)) {
            throw new UnauthorizedException("잘못된 인증 주체입니다.");
        }

        return user;
    }

    /** 현재 로그인한 사용자 (SecurityUser) */
    public static SecurityUser getCurrentUser() {
        return getRequiredUser();
    }

    /** 현재 로그인한 사용자의 회사 ID */
    public static Long getCompanyId() {
        return getRequiredUser().getCompanyId();
    }

    /** 현재 로그인한 사용자의 사원 ID */
    public static Long getEmployeeId() {
        return getRequiredUser().getEmployeeId();
    }
}
