package com.infyro.ifc.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class CsrfCookieValidatorTest {
    private final TokenCookieService cookies = new TokenCookieService(false, "Lax", "test");
    private final CsrfCookieValidator validator = new CsrfCookieValidator(cookies, "http://localhost:5173");

    @Test
    void acceptsMatchingDoubleSubmitTokenFromConfiguredOrigin() {
        MockHttpServletRequest request = requestWithMatchingTokens();
        request.addHeader("Origin", "http://localhost:5173");
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void rejectsMissingOriginEvenWhenTokensMatch() {
        MockHttpServletRequest request = requestWithMatchingTokens();
        assertForbidden(request);
    }

    @Test
    void rejectsUntrustedOriginEvenWhenTokensMatch() {
        MockHttpServletRequest request = requestWithMatchingTokens();
        request.addHeader("Origin", "https://attacker.example");
        assertForbidden(request);
    }

    @Test
    void rejectsMissingOrMismatchedTokens() {
        MockHttpServletRequest missing = new MockHttpServletRequest("POST", "/ifc/v1/auth/refresh");
        missing.addHeader("Origin", "http://localhost:5173");
        assertForbidden(missing);

        MockHttpServletRequest mismatch = new MockHttpServletRequest("POST", "/ifc/v1/auth/refresh");
        mismatch.setCookies(new Cookie(TokenCookieService.CSRF_COOKIE, "cookie-token"));
        mismatch.addHeader("X-XSRF-TOKEN", "different-token");
        mismatch.addHeader("Origin", "http://localhost:5173");
        assertForbidden(mismatch);
    }

    private MockHttpServletRequest requestWithMatchingTokens() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/ifc/v1/auth/refresh");
        request.setCookies(new Cookie(TokenCookieService.CSRF_COOKIE, "same-token"));
        request.addHeader("X-XSRF-TOKEN", "same-token");
        return request;
    }

    private void assertForbidden(MockHttpServletRequest request) {
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> validator.validate(request)).getStatusCode().value());
    }
}
