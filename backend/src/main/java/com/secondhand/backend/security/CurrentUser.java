package com.secondhand.backend.security;

import com.secondhand.backend.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Small helper used by controllers to read the logged-in user
 * from the request attributes (filled by JwtFilter).
 */
public class CurrentUser {

    private CurrentUser() {
        // utility class, no instances
    }

    /** Returns the id of the logged-in user or throws 401 error. */
    public static Long requireUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            throw new ApiException(401, "ابتدا باید وارد حساب کاربری خود شوید");
        }
        return (Long) userId;
    }

    /** Makes sure the logged-in user is an admin, otherwise throws 403 error. */
    public static Long requireAdmin(HttpServletRequest request) {
        Long userId = requireUserId(request);
        Object role = request.getAttribute("role");
        if (role == null || !role.equals("ADMIN")) {
            throw new ApiException(403, "فقط مدیر سامانه به این بخش دسترسی دارد");
        }
        return userId;
    }
}
