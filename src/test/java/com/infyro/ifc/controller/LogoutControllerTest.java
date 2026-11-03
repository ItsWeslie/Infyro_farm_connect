package com.infyro.ifc.controller;

import com.infyro.ifc.controller.auth.LogoutController;
import com.infyro.ifc.security.CsrfCookieValidator;
import com.infyro.ifc.security.TokenCookieService;
import com.infyro.ifc.service.login.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogoutControllerTest {
    @Test
    void logoutAllRevokesSessionsAndClearsBothCookies() {
        RefreshTokenService sessions = mock(RefreshTokenService.class);
        TokenCookieService cookies = new TokenCookieService(false, "Lax", "test");
        LogoutController controller = new LogoutController(sessions, mock(CsrfCookieValidator.class), cookies);
        var authentication = new UsernamePasswordAuthenticationToken("user@example.com", "n/a");

        var response = controller.logoutAll(authentication);

        assertEquals(204, response.getStatusCode().value());
        verify(sessions).logoutAll("user@example.com");
        var setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookies);
        assertEquals(2, setCookies.size());
        assertTrue(setCookies.stream().anyMatch(value -> value.contains("refresh_token=")
                && value.contains("Path=/ifc/v1/auth")));
        assertTrue(setCookies.stream().anyMatch(value -> value.contains("XSRF-TOKEN=")
                && value.contains("Path=/;")));
    }
}
