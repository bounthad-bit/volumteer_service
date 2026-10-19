package com.volunteer.security;

import com.volunteer.common.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static LoginUser currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser)) {
            return null;
        }
        return (LoginUser) auth.getPrincipal();
    }

    /** 取当前登录用户，未登录直接抛业务异常。 */
    public static LoginUser currentUser() {
        LoginUser u = currentUserOrNull();
        if (u == null) {
            throw new BizException("请先登录");
        }
        return u;
    }

    public static Long currentUserId() {
        return currentUser().getUserId();
    }

    public static Integer currentRole() {
        return currentUser().getRole();
    }

    public static boolean isAdmin() {
        LoginUser u = currentUserOrNull();
        return u != null && Integer.valueOf(3).equals(u.getRole());
    }

    public static boolean isOrganizer() {
        LoginUser u = currentUserOrNull();
        return u != null && Integer.valueOf(2).equals(u.getRole());
    }

    public static boolean isVolunteer() {
        LoginUser u = currentUserOrNull();
        return u != null && Integer.valueOf(1).equals(u.getRole());
    }
}
