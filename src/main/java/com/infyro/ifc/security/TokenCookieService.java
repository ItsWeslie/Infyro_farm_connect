package com.infyro.ifc.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
public class TokenCookieService {
    public static final String REFRESH_COOKIE = "refresh_token";
    public static final String CSRF_COOKIE = "XSRF-TOKEN";
    private static final String REFRESH_PATH = "/ifc/v1/auth";
    private static final String CSRF_PATH = "/";
    private final boolean secure;
    private final String sameSite;

    public TokenCookieService(@Value("${app.cookie.secure:false}") boolean secure,
                              @Value("${app.cookie.same-site:Lax}") String sameSite,
                              @Value("${spring.profiles.active:}") String activeProfiles) {
        if (!secure && Arrays.stream(activeProfiles.split(","))
                .map(String::trim).anyMatch(profile -> profile.equalsIgnoreCase("prod")
                        || profile.equalsIgnoreCase("production"))) {
            throw new IllegalStateException("app.cookie.secure must be true in production");
        }
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public ResponseCookie refreshCookie(String token, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, token).httpOnly(true).secure(secure)
                .sameSite(sameSite).path(REFRESH_PATH).maxAge(maxAge).build();
    }

    public ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE, "").httpOnly(true).secure(secure)
                .sameSite(sameSite).path(REFRESH_PATH).maxAge(Duration.ZERO).build();
    }

    public ResponseCookie csrfCookie(String token, Duration maxAge) {
        return ResponseCookie.from(CSRF_COOKIE, token).httpOnly(false).secure(secure)
                .sameSite(sameSite).path(CSRF_PATH).maxAge(maxAge).build();
    }

    public ResponseCookie clearCsrfCookie() {
        return ResponseCookie.from(CSRF_COOKIE, "").httpOnly(false).secure(secure)
                .sameSite(sameSite).path(CSRF_PATH).maxAge(Duration.ZERO).build();
    }

    public String refreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_COOKIE.equals(c.getName()))
                .map(jakarta.servlet.http.Cookie::getValue).findFirst().orElse(null);
    }

    public String csrfCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> CSRF_COOKIE.equals(c.getName()))
                .map(jakarta.servlet.http.Cookie::getValue).findFirst().orElse(null);
    }
}