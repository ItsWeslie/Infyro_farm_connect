package com.infyro.ifc.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;

@Component
public class CsrfCookieValidator {
    private final TokenCookieService cookies;
    public CsrfCookieValidator(TokenCookieService cookies) { this.cookies = cookies; }

    public void validate(HttpServletRequest request) {
        String cookie = cookies.csrfCookie(request);
        String header = request.getHeader("X-XSRF-TOKEN");
        if (cookie == null || header == null ||
                !MessageDigest.isEqual(cookie.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        header.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CSRF validation failed");
        }
    }
}