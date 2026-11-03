package com.infyro.ifc.service.login;

import com.infyro.ifc.enums.UserType;
import com.infyro.ifc.exception.InvalidTokenException;
import com.infyro.ifc.model.RefreshToken;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.RefreshTokenRepo;
import com.infyro.ifc.repository.UserRepo;
import com.infyro.ifc.security.JWTService;
import com.infyro.ifc.security.TokenCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RefreshTokenRepo tokens;
    private final UserRepo users;
    private final JWTService jwt;
    private final TokenCookieService cookies;
    private final Duration idleLifetime;
    private final Duration absoluteLifetime;
    private final Duration accessLifetime;

    public RefreshTokenService(RefreshTokenRepo tokens, UserRepo users, JWTService jwt,
            TokenCookieService cookies,
            @Value("${auth.refresh.idle-lifetime:PT168H}") Duration idleLifetime,
            @Value("${auth.refresh.absolute-lifetime:P30D}") Duration absoluteLifetime,
            @Value("${jwt.expiration:PT10M}") Duration accessLifetime) {
        this.tokens = tokens; this.users = users; this.jwt = jwt; this.cookies = cookies;
        this.idleLifetime = idleLifetime; this.absoluteLifetime = absoluteLifetime;
        this.accessLifetime = accessLifetime;
    }

    @Transactional
    public ResponseEntity<com.infyro.ifc.dto.auth.TokenResponse> issue(User user) {
        Instant now = Instant.now();
        String family = UUID.randomUUID().toString();
        Instant absoluteExpiry = now.plus(absoluteLifetime);
        String raw = newToken();
        tokens.save(RefreshToken.builder().user(user).tokenHash(hash(raw)).familyId(family)
                .createdAt(now).expiresAt(min(now.plus(idleLifetime), absoluteExpiry))
                .absoluteExpiresAt(absoluteExpiry).build());
        String csrf = newToken();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                cookies.refreshCookie(raw, Duration.between(now, min(now.plus(idleLifetime), absoluteExpiry))).toString())
                .header(HttpHeaders.SET_COOKIE, cookies.csrfCookie(csrf, Duration.between(now, absoluteExpiry)).toString())
                .body(response(user, family));
    }

    @Transactional
    public ResponseEntity<com.infyro.ifc.dto.auth.TokenResponse> rotate(HttpServletRequest request) {
        String raw = cookies.refreshToken(request);
        if (raw == null || raw.isBlank()) throw unauthorized();
        Instant now = Instant.now();
        RefreshToken current = tokens.findForUpdateByTokenHash(hash(raw)).orElseThrow(this::unauthorized);
        if (current.getRevokedAt() != null) {
            tokens.revokeFamily(current.getFamilyId(), now);
            throw unauthorized();
        }
        if (!current.getExpiresAt().isAfter(now) || !current.getAbsoluteExpiresAt().isAfter(now)) {
            tokens.revokeFamily(current.getFamilyId(), now);
            throw unauthorized();
        }
        current.setRevokedAt(now);
        current.setLastUsedAt(now);
        String nextRaw = newToken();
        RefreshToken next = tokens.save(RefreshToken.builder().user(current.getUser())
                .tokenHash(hash(nextRaw)).familyId(current.getFamilyId()).createdAt(now)
                .expiresAt(min(now.plus(idleLifetime), current.getAbsoluteExpiresAt()))
                .absoluteExpiresAt(current.getAbsoluteExpiresAt()).build());
        current.setReplacedById(next.getId());
        User user = current.getUser();
        String csrf = newToken();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                cookies.refreshCookie(nextRaw, Duration.between(now, next.getExpiresAt())).toString())
                .header(HttpHeaders.SET_COOKIE, cookies.csrfCookie(csrf,
                        Duration.between(now, next.getAbsoluteExpiresAt())).toString())
                .body(response(user, current.getFamilyId()));
    }

    @Transactional
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String raw = cookies.refreshToken(request);
        if (raw != null) tokens.findForUpdateByTokenHash(hash(raw))
                .ifPresent(t -> tokens.revokeFamily(t.getFamilyId(), Instant.now()));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.clearRefreshCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookies.clearCsrfCookie().toString()).build();
    }

    @Transactional
    public void logoutAll(String email) {
        User user = users.findUserByEmail(email).orElseThrow(this::unauthorized);
        tokens.revokeAllForUser(user.getId(), Instant.now());
    }

    public boolean isFamilyActive(String family) {
        return family != null && tokens.existsByFamilyIdAndRevokedAtIsNullAndExpiresAtAfter(family, Instant.now());
    }

    private com.infyro.ifc.dto.auth.TokenResponse response(User user, String family) {
        return new com.infyro.ifc.dto.auth.TokenResponse(jwt.generateToken(user.getEmail(), family),
                accessLifetime.toSeconds(), user.getEmail(), UserType.USER, "Authentication successful", true);
    }
    private static String newToken() { byte[] b = new byte[48]; RANDOM.nextBytes(b); return Base64.getUrlEncoder().withoutPadding().encodeToString(b); }
    private static String hash(String raw) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException("SHA-256 unavailable", e); }
    }
    private static Instant min(Instant a, Instant b) { return a.isBefore(b) ? a : b; }
    private ResponseStatusException unauthorized() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired authentication session"); }
}