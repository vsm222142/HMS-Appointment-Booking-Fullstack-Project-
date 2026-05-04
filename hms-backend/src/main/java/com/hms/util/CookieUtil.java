package com.hms.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {
    public static final String AUTH_COOKIE = "HMS_TOKEN";

    public static Cookie httpOnlyAuthCookie(String token, boolean secure, int maxAgeSeconds) {
        Cookie cookie = new Cookie(AUTH_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    public static Cookie clearAuthCookie(boolean secure) {
        Cookie cookie = new Cookie(AUTH_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}

