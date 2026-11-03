package com.infyro.ifc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JWTService {
    private final String secret;
    private final long expiration;
    private final String issuer;
    private final String audience;

    public JWTService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration:PT10M}") java.time.Duration expiration,
                      @Value("${jwt.issuer:infyro-farm-connect}") String issuer,
                      @Value("${jwt.audience:infyro-api}") String audience) {
        this.secret = secret; this.expiration = expiration.toMillis();
        this.issuer = issuer; this.audience = audience;
    }
    private Key key() { return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)); }

    public String generateToken(String username) { return generateToken(username, UUID.randomUUID().toString()); }
    public String generateToken(String username, String sessionId) {
        Date now = new Date();
        return Jwts.builder().subject(username).issuer(issuer).audience().add(audience).and()
                .id(UUID.randomUUID().toString()).claim("sid", sessionId)
                .issuedAt(now).expiration(new Date(now.getTime() + expiration))
                .signWith(key()).compact();
    }
    public String extractUsername(String token) { return extractClaim(token, Claims::getSubject); }
    public String extractSessionId(String token) { return extractClaim(token, c -> c.get("sid", String.class)); }
    private <T> T extractClaim(String token, Function<Claims,T> resolver) { return resolver.apply(parse(token)); }
    private Claims parse(String token) {
        try { return Jwts.parser().verifyWith((javax.crypto.SecretKey) key()).requireIssuer(issuer)
                .requireAudience(audience).build().parseSignedClaims(token).getPayload(); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid or expired token"); }
    }
    public boolean validateToken(String token, UserDetails user) {
        Claims claims = parse(token);
        return claims.getSubject().equals(user.getUsername()) && claims.getExpiration().after(new Date())
                && claims.getId() != null && claims.get("sid", String.class) != null;
    }
    public boolean validateToken(String token, String username) {
        Claims claims = parse(token);
        return claims.getSubject().equals(username) && claims.getExpiration().after(new Date());
    }
}