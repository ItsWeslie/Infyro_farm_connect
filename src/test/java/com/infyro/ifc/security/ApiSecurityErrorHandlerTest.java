package com.infyro.ifc.security;

import tools.jackson.databind.ObjectMapper;
import com.infyro.ifc.dto.ApiErrorResponse;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiSecurityErrorHandlerTest {

    private final SecurityConfig config = new SecurityConfig(null);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void unauthenticatedRequestGetsStructuredJson401() throws IOException, ServletException {
        AuthenticationEntryPoint entryPoint = config.apiAuthenticationEntryPoint(objectMapper);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(new MockHttpServletRequest(), response,
                new AuthenticationException("not authenticated") { });

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        ApiErrorResponse body = objectMapper.readValue(response.getContentAsByteArray(), ApiErrorResponse.class);
        assertEquals(401, body.status());
        assertEquals("UNAUTHORIZED", body.code());
        assertNotNull(body.message());
    }

    @Test
    void forbiddenRequestGetsStructuredJson403() throws IOException, ServletException {
        AccessDeniedHandler handler = config.apiAccessDeniedHandler(objectMapper);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response,
                new AccessDeniedException("forbidden"));

        assertEquals(403, response.getStatus());
        assertEquals("application/json", response.getContentType());
        ApiErrorResponse body = objectMapper.readValue(response.getContentAsByteArray(), ApiErrorResponse.class);
        assertEquals(403, body.status());
        assertEquals("FORBIDDEN", body.code());
        assertNotNull(body.message());
    }
}
