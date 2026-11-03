package com.infyro.ifc.service.login;

import com.infyro.ifc.model.RefreshToken;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.RefreshTokenRepo;
import com.infyro.ifc.repository.UserRepo;
import com.infyro.ifc.security.JWTService;
import com.infyro.ifc.security.TokenCookieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {
    private RefreshTokenRepo tokens;
    private UserRepo users;
    private JWTService jwt;
    private TokenCookieService cookies;
    private RefreshTokenService service;
    private User user;

    @BeforeEach
    void setUp() {
        tokens = mock(RefreshTokenRepo.class);
        users = mock(UserRepo.class);
        jwt = mock(JWTService.class);
        cookies = new TokenCookieService(false, "Lax", "test");
        service = new RefreshTokenService(tokens, users, jwt, cookies,
                Duration.ofDays(7), Duration.ofDays(30), Duration.ofMinutes(10));
        user = User.builder().id(7L).username("test-user").email("test@example.com")
                .password("hashed").build();
        when(jwt.generateToken(anyString(), anyString())).thenReturn("access.jwt.token");
    }

    @Test
    void rotatingRefreshTokenRevokesOldTokenAndIssuesReplacement() throws Exception {
        Instant now = Instant.now();
        RefreshToken old = RefreshToken.builder().id(11L).user(user).familyId("family-1")
                .tokenHash(hash("known-refresh-secret")).createdAt(now.minusSeconds(60))
                .expiresAt(now.plus(Duration.ofDays(6))).absoluteExpiresAt(now.plus(Duration.ofDays(20))).build();
        when(tokens.findForUpdateByTokenHash(hash("known-refresh-secret"))).thenReturn(Optional.of(old));
        when(tokens.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken next = invocation.getArgument(0);
            next.setId(12L);
            return next;
        });
        MockHttpServletRequest request = requestWithRefresh("known-refresh-secret");

        var response = service.rotate(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(old.getRevokedAt());
        assertEquals(12L, old.getReplacedById());
        assertEquals(2, response.getHeaders().get(HttpHeaders.SET_COOKIE).size());
        verify(tokens, never()).revokeFamily(eq("family-1"), any());
    }

    @Test
    void replayOfRotatedTokenRevokesItsEntireFamily() throws Exception {
        Instant now = Instant.now();
        RefreshToken replayed = RefreshToken.builder().id(11L).user(user).familyId("family-1")
                .tokenHash(hash("replayed-secret")).createdAt(now.minusSeconds(60))
                .expiresAt(now.plus(Duration.ofDays(6))).absoluteExpiresAt(now.plus(Duration.ofDays(20)))
                .revokedAt(now.minusSeconds(1)).build();
        when(tokens.findForUpdateByTokenHash(hash("replayed-secret"))).thenReturn(Optional.of(replayed));

        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.rotate(requestWithRefresh("replayed-secret"))).getStatusCode().value());
        verify(tokens).revokeFamily(eq("family-1"), any(Instant.class));
    }

    @Test
    void expiredRefreshTokenRevokesItsFamily() throws Exception {
        Instant now = Instant.now();
        RefreshToken expired = RefreshToken.builder().id(11L).user(user).familyId("family-1")
                .tokenHash(hash("expired-secret")).createdAt(now.minus(Duration.ofDays(8)))
                .expiresAt(now.minusSeconds(1)).absoluteExpiresAt(now.plus(Duration.ofDays(10))).build();
        when(tokens.findForUpdateByTokenHash(hash("expired-secret"))).thenReturn(Optional.of(expired));

        assertEquals(401, assertThrows(ResponseStatusException.class,
                () -> service.rotate(requestWithRefresh("expired-secret"))).getStatusCode().value());
        verify(tokens).revokeFamily(eq("family-1"), any(Instant.class));
    }

    private MockHttpServletRequest requestWithRefresh(String raw) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/ifc/v1/auth/refresh");
        request.setCookies(new Cookie(TokenCookieService.REFRESH_COOKIE, raw));
        return request;
    }

    private static String hash(String raw) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(raw.getBytes(StandardCharsets.UTF_8)));
    }
}
