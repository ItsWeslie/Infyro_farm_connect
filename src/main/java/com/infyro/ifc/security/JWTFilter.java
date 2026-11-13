package com.infyro.ifc.security;

import com.infyro.ifc.service.login.RefreshTokenService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final ApplicationContext context;
    private final RefreshTokenService sessions;

    public JWTFilter(JWTService jwtService,
                     ApplicationContext context,
                     RefreshTokenService sessions) {

        this.jwtService = jwtService;
        this.context = context;
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(header.substring(7), request);
        }

        chain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            String username = jwtService.extractUsername(token);
            String family = jwtService.extractSessionId(token);

            if (username == null || username.isBlank() || !sessions.isFamilyActive(family)) {
                SecurityContextHolder.clearContext();
                return;
            }

            UserDetails details = context.getBean(UsersDetailService.class).loadUserByUsername(username);
            if (jwtService.validateToken(token, details)) {

                var auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            }
        } catch (RuntimeException e) {
            log.debug("Ignoring invalid, expired, revoked, or unknown-user access token");
            SecurityContextHolder.clearContext();
        }
    }
}