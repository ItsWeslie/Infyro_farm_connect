package com.infyro.ifc.controller.auth;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.RefreshTokenRepo;
import com.infyro.ifc.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {
    private static final String EMAIL = "auth-flow@example.com";
    private static final String PASSWORD = "StrongPass1!";
    private static final String ORIGIN = "http://localhost:5173";
    private static final String LOGIN = "/ifc/v1/auth/login";
    private static final String REFRESH = "/ifc/v1/auth/refresh";
    private static final String LOGOUT = "/ifc/v1/auth/logout";
    private static final String VALIDATE = "/ifc/v1/auth/validate";

    @Autowired private MockMvc mvc;
    @Autowired private UserRepo users;
    @Autowired private RefreshTokenRepo refreshTokens;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper mapper;

    @BeforeEach
    void createUser() {
        refreshTokens.deleteAll();
        users.deleteAll();
        users.saveAndFlush(User.builder()
                .username("Auth Flow User")
                .email(EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .build());
    }

    @Test
    void loginRefreshAndReplayRevokesTheEntireSessionFamily() throws Exception {
        JsonNode login = login();
        String oldRefresh = cookieValue(loginResultCookies, "refresh_token");
        String oldCsrf = cookieValue(loginResultCookies, "XSRF-TOKEN");
        String oldAccess = login.get("accessToken").asText();

        MvcResult rotated = mvc.perform(post(REFRESH)
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .header("X-XSRF-TOKEN", oldCsrf)
                        .cookie(new Cookie("refresh_token", oldRefresh), new Cookie("XSRF-TOKEN", oldCsrf)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        List<String> rotatedCookies = rotated.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        String newRefresh = cookieValue(rotatedCookies, "refresh_token");
        String newCsrf = cookieValue(rotatedCookies, "XSRF-TOKEN");
        String newAccess = mapper.readTree(rotated.getResponse().getContentAsString())
                .get("accessToken").asText();
        assertNotEquals(oldRefresh, newRefresh);

        // Reusing the rotated-away credential must revoke the complete family.
        mvc.perform(post(REFRESH)
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .header("X-XSRF-TOKEN", oldCsrf)
                        .header(HttpHeaders.COOKIE, cookieHeader(oldRefresh, oldCsrf)))
                .andExpect(status().isUnauthorized());

        mvc.perform(get(VALIDATE).header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccess))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(VALIDATE).header(HttpHeaders.AUTHORIZATION, "Bearer " + oldAccess))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesSessionAndClearsCookies() throws Exception {
        JsonNode login = login();
        String refresh = cookieValue(loginResultCookies, "refresh_token");
        String csrf = cookieValue(loginResultCookies, "XSRF-TOKEN");
        String access = login.get("accessToken").asText();

        mvc.perform(post(LOGOUT)
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .header("X-XSRF-TOKEN", csrf)
                        .cookie(new Cookie("refresh_token", refresh), new Cookie("XSRF-TOKEN", csrf)))
                .andExpect(status().isNoContent());

        mvc.perform(get(VALIDATE).header(HttpHeaders.AUTHORIZATION, "Bearer " + access))
                .andExpect(status().isUnauthorized());
    }

    private List<String> loginResultCookies;

    private JsonNode login() throws Exception {
        MvcResult result = mvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s","loginType":"jwt"}
                                """.formatted(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();
        loginResultCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertNotNull(loginResultCookies);
        assertEquals(2, loginResultCookies.size());
        return mapper.readTree(result.getResponse().getContentAsString());
    }

    private String cookieValue(List<String> setCookies, String name) {
        return setCookies.stream()
                .filter(value -> value.startsWith(name + "="))
                .findFirst()
                .map(value -> value.substring(name.length() + 1, value.indexOf(';')))
                .orElseThrow(() -> new AssertionError("Missing " + name + " cookie"));
    }

}
