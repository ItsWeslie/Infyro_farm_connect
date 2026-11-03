package com.infyro.ifc.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class CsrfCookieValidator {
    private final TokenCookieService cookies;
    private final String allowedOrigin;

    public CsrfCookieValidator(TokenCookieService cookies,
                               @Value("${app.cors.allowed-origin:http://localhost:5173}") String allowedOrigin) {
        this.cookies = cookies;
        this.allowedOrigin = allowedOrigin;
    }

    public void validate(HttpServletRequest request) {
        // Browsers send Origin on cross-origin POST requests. Reject mismatches even
        // when an attacker can cause a request to carry a valid ambient cookie.
        String origin = request.getHeader("Origin");
        if (origin != null && !allowedOrigin.equals(origin)) {
            throw forbidden();
        }

        String cookie = cookies.csrfCookie(request);
        String header = request.getHeader("X-XSRF-TOKEN");
        if (cookie == null || header == null ||
                !MessageDigest.isEqual(cookie.getBytes(StandardCharsets.UTF_8),
                        header.getBytes(StandardCharsets.UTF_8))) {
            throw forbidden();
        }
    }

    private ResponseStatusException forbidden() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "CSRF validation failed");
    }
}