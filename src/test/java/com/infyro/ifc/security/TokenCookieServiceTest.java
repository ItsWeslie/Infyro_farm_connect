package com.infyro.ifc.security;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class TokenCookieServiceTest {
    @Test
    void csrfCookieIsReadableAtSpaRootWhileRefreshCookieIsHttpOnlyAndScoped() {
        TokenCookieService cookies = new TokenCookieService(false, "Lax", "test");
        var csrf = cookies.csrfCookie("csrf-value", Duration.ofHours(1)).toString();
        var refresh = cookies.refreshCookie("refresh-value", Duration.ofHours(1)).toString();

        assertTrue(csrf.contains("Path=/;"));
        assertFalse(csrf.contains("HttpOnly"));
        assertTrue(refresh.contains("Path=/ifc/v1/auth"));
        assertTrue(refresh.contains("HttpOnly"));
    }

    @Test
    void productionRefusesInsecureCookies() {
        assertThrows(IllegalStateException.class,
                () -> new TokenCookieService(false, "Lax", "prod"));
        assertDoesNotThrow(() -> new TokenCookieService(true, "Lax", "production"));
    }

    @Test
    void clearingCookiesUsesTheirOriginalPaths() {
        TokenCookieService cookies = new TokenCookieService(false, "Lax", "test");
        assertTrue(cookies.clearRefreshCookie().toString().contains("Path=/ifc/v1/auth"));
        assertTrue(cookies.clearCsrfCookie().toString().contains("Path=/;"));
    }
}
