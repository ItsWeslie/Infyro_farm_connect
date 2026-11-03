package com.infyro.ifc.security;

import com.infyro.ifc.exception.UserNotFoundException;
import com.infyro.ifc.service.login.RefreshTokenService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class JWTFilterTest {

    private final JWTService jwtService = mock(JWTService.class);
    private final ApplicationContext context = mock(ApplicationContext.class);
    private final UsersDetailService usersDetailService = mock(UsersDetailService.class);
    private final RefreshTokenService sessions = mock(RefreshTokenService.class);
    private final JWTFilter filter = new JWTFilter(jwtService, context, sessions);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void invalidTokenDoesNotEscapeFilterAndLeavesRequestUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/farms");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(jwtService.extractUsername("invalid-token")).thenThrow(new RuntimeException("invalid"));

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void tokenForDeletedUserDoesNotEscapeFilterAndLeavesRequestUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/farms");
        request.addHeader("Authorization", "Bearer stale-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(jwtService.extractUsername("stale-token")).thenReturn("deleted@example.com");
        when(jwtService.extractSessionId("stale-token")).thenReturn("family-1");
        when(sessions.isFamilyActive("family-1")).thenReturn(true);
        when(context.getBean(UsersDetailService.class)).thenReturn(usersDetailService);
        when(usersDetailService.loadUserByUsername("deleted@example.com"))
                .thenThrow(new UserNotFoundException("no longer exists"));

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void usernameNotFoundExceptionDoesNotEscapeFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/farms");
        request.addHeader("Authorization", "Bearer stale-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        when(jwtService.extractUsername("stale-token")).thenReturn("deleted@example.com");
        when(jwtService.extractSessionId("stale-token")).thenReturn("family-1");
        when(sessions.isFamilyActive("family-1")).thenReturn(true);
        when(context.getBean(UsersDetailService.class)).thenReturn(usersDetailService);
        when(usersDetailService.loadUserByUsername("deleted@example.com"))
                .thenThrow(new UsernameNotFoundException("no longer exists"));

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
